import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:media_kit/media_kit.dart';
import 'package:media_kit_video/media_kit_video.dart';
import 'package:window_manager/window_manager.dart';
import 'dart:async';
import 'dart:io';
import 'dart:convert';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as io;
import 'package:shelf_router/shelf_router.dart' as shelf_router;

import 'api/bridge_client.dart';
import 'models/bridge_models.dart';
import 'providers/intro_outro_provider.dart';
import 'ui/components/custom_ui.dart';
import 'ui/components/settings_menu.dart';

const int kPlayerPort = 47920;

void main(List<String> args) async {
  WidgetsFlutterBinding.ensureInitialized();
  MediaKit.ensureInitialized();
  await windowManager.ensureInitialized();

  String? url;
  String? title;
  Duration startPosition = Duration.zero;

  if (args.isNotEmpty) {
    url = args[0];
    if (args.length > 1) {
      title = args[1];
    }
    if (args.length > 2) {
      try {
        final posMs = int.parse(args[2]);
        startPosition = Duration(milliseconds: posMs);
      } catch (e) {
        debugPrint('Error parsing start position: $e');
      }
    }
  }

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => IntroOutroProvider()),
        Provider(create: (_) => BridgeApiClient()),
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        theme: ThemeData.dark(useMaterial3: true).copyWith(
          scaffoldBackgroundColor: Colors.black,
          colorScheme: const ColorScheme.dark(
            primary: Colors.white,
            secondary: Colors.blueAccent,
          ),
        ),
        home: PlayerScreen(url: url, title: title, startPosition: startPosition),
      ),
    ),
  );
}

class PlayerScreen extends StatefulWidget {
  final String? url;
  final String? title;
  final Duration startPosition;

  const PlayerScreen({
    super.key,
    this.url,
    this.title,
    this.startPosition = Duration.zero,
  });

  @override
  State<PlayerScreen> createState() => _PlayerScreenState();
}

class _PlayerScreenState extends State<PlayerScreen> with WindowListener {
  late final Player player = Player();
  late final VideoController controller = VideoController(player);
  
  bool _showControls = true;
  Timer? _hideControlsTimer;
  bool _isHovering = false;
  HttpServer? _server;
  
  // State for Settings
  bool _isSettingsOpen = false;
  String _aspectRatio = "默认";
  String _windowRatio = "自动";
  bool _isSmallWindow = false;
  
  // Bridge Data
  AppSettings? _appSettings;
  PlayerSettings? _playerSettings;

  @override
  void initState() {
    super.initState();
    windowManager.addListener(this);
    _initPlayer();
    _startHttpServer();
    _initWindow();
    _fetchBridgeSettings();
  }

  Future<void> _fetchBridgeSettings() async {
    try {
      final client = context.read<BridgeApiClient>();
      final appSettings = await client.getAppSettings();
      final playerSettings = await client.getPlayerSettings();
      setState(() {
        _appSettings = appSettings;
        _playerSettings = playerSettings;
        // Apply volume
        player.setVolume(playerSettings.volume * 100);
      });
    } catch (e) {
      debugPrint("Failed to fetch settings from bridge: $e");
    }
  }

  Future<void> _initWindow() async {
    WindowOptions windowOptions = WindowOptions(
      size: const Size(1280, 720),
      center: true,
      backgroundColor: Colors.transparent,
      skipTaskbar: false,
      titleBarStyle: TitleBarStyle.hidden,
      title: widget.title ?? 'FnTV Player',
    );

    await windowManager.waitUntilReadyToShow(windowOptions, () async {
      await windowManager.show();
      await windowManager.focus();
    });
  }

  Future<void> _startHttpServer() async {
    final router = shelf_router.Router();

    router.post('/play', (Request request) async {
      final payload = await request.readAsString();
      final data = jsonDecode(payload);
      
      final String url = data['url'];
      final String? title = data['title'];
      final int startPosMs = data['startPos'] ?? 0;

      if (mounted) {
        if (title != null) {
          windowManager.setTitle(title);
        }
        windowManager.show();
        windowManager.focus();
        await _playNewMedia(url, Duration(milliseconds: startPosMs));
      }
      return Response.ok(jsonEncode({'status': 'ok'}));
    });

    try {
      _server = await io.serve(router, InternetAddress.loopbackIPv4, kPlayerPort);
    } catch (e) {
      debugPrint('Failed to start HTTP server: $e');
    }
  }

  Future<void> _playNewMedia(String url, Duration startPosition) async {
    final Completer<void> readyToSeek = Completer<void>();
    final StreamSubscription<Duration> subscription = player.stream.duration.listen((Duration d) {
      if (d > Duration.zero && !readyToSeek.isCompleted) {
        readyToSeek.complete();
      }
    });

    try {
      await player.open(Media(url), play: false);

      if (startPosition > Duration.zero) {
        await readyToSeek.future.timeout(
          const Duration(seconds: 5),
          onTimeout: () {},
        );
        await player.seek(startPosition);
      }
    } catch (e) {
      debugPrint('Error during media change: $e');
    } finally {
      await subscription.cancel();
    }

    await player.play();
    _onUserInteraction();
  }

  Future<void> _initPlayer() async {
    if (widget.url != null) {
      await _playNewMedia(widget.url!, widget.startPosition);
    }
    _startHideControlsTimer();
  }

  void _startHideControlsTimer() {
    _hideControlsTimer?.cancel();
    _hideControlsTimer = Timer(const Duration(seconds: 3), () {
      if (mounted && !_isHovering && player.state.playing && !_isSettingsOpen) {
        setState(() {
          _showControls = false;
        });
      }
    });
  }

  void _onUserInteraction() {
    setState(() {
      _showControls = true;
    });
    _startHideControlsTimer();
  }

  @override
  void dispose() {
    windowManager.removeListener(this);
    _hideControlsTimer?.cancel();
    player.dispose();
    _server?.close();
    super.dispose();
  }

  void _toggleSmallWindow() async {
    setState(() {
      _isSmallWindow = !_isSmallWindow;
    });
    
    if (_isSmallWindow) {
      await windowManager.setAlwaysOnTop(true);
      await windowManager.setSize(const Size(480, 270));
      await windowManager.setAlignment(Alignment.bottomRight);
    } else {
      await windowManager.setAlwaysOnTop(false);
      await windowManager.setSize(const Size(1280, 720)); // Restore default or saved size
      await windowManager.center();
    }
  }

  void _toggleFullScreen() {
    windowManager.isFullScreen().then((isFull) {
      windowManager.setFullScreen(!isFull);
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isSmallWindow) {
      return _buildSmallWindowUI();
    }

    return RawKeyboardListener(
      focusNode: FocusNode()..requestFocus(),
      onKey: (event) {
        if (event is RawKeyDownEvent) {
          _onUserInteraction();
          // Handle shortcuts (Space, Arrows, etc.)
        }
      },
      child: Scaffold(
        backgroundColor: Colors.black,
        body: MouseRegion(
          onHover: (_) {
            _isHovering = true;
            _onUserInteraction();
          },
          onExit: (_) {
            _isHovering = false;
            _startHideControlsTimer();
          },
          child: Stack(
            children: [
              GestureDetector(
                onTap: () {
                  if (_isSettingsOpen) {
                    setState(() => _isSettingsOpen = false);
                  } else {
                    _onUserInteraction();
                  }
                },
                onDoubleTap: _toggleFullScreen,
                child: Center(
                  child: Video(
                    controller: controller,
                    controls: NoVideoControls,
                  ),
                ),
              ),
              
              // Loading Indicator
              Center(
                child: StreamBuilder<bool>(
                  stream: player.stream.buffering,
                  builder: (context, snapshot) {
                    if (snapshot.data == true) {
                      return const CircularLoadingIndicator();
                    }
                    return const SizedBox.shrink();
                  },
                ),
              ),

              // UI Overlay
              if (_showControls) ...[
                // Top Bar
                Positioned(
                  top: 0, left: 0, right: 0,
                  child: DragToMoveArea(
                    child: Container(
                      height: 60,
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [Colors.black.withOpacity(0.8), Colors.transparent],
                        ),
                      ),
                      child: Row(
                        children: [
                          IconButton(
                            icon: const Icon(Icons.arrow_back, color: Colors.white),
                            onPressed: () => exit(0),
                          ),
                          Expanded(
                            child: Text(
                              widget.title ?? 'FnTV Player',
                              style: const TextStyle(color: Colors.white, fontSize: 16),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),

                // Bottom Bar
                Positioned(
                  bottom: 0, left: 0, right: 0,
                  child: _buildBottomControls(),
                ),
              ],

              // Settings Menu Overlay
              if (_isSettingsOpen)
                Positioned(
                  bottom: 80,
                  right: 16,
                  child: SettingsMenu(
                    isTvEpisode: true, // TODO: Determine from metadata
                    currentAspectRatio: _aspectRatio,
                    currentWindowRatio: _windowRatio,
                    onAspectRatioChanged: (val) {
                      setState(() => _aspectRatio = val);
                      // Apply aspect ratio logic to player/window
                    },
                    onWindowRatioChanged: (val) {
                      setState(() => _windowRatio = val);
                      // Apply window ratio logic
                    },
                    onIntroOutroTap: () {
                      setState(() => _isSettingsOpen = false);
                      _showIntroOutroDialog();
                    },
                    onClose: () => setState(() => _isSettingsOpen = false),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildBottomControls() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.bottomCenter,
          end: Alignment.topCenter,
          colors: [Colors.black.withOpacity(0.9), Colors.transparent],
        ),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Progress Bar
          StreamBuilder<Duration>(
            stream: player.stream.position,
            builder: (context, snapshot) {
              final position = snapshot.data ?? Duration.zero;
              final duration = player.state.duration;
              final buffer = player.state.buffer;
              
              double progress = 0.0;
              double buffered = 0.0;
              if (duration.inMilliseconds > 0) {
                progress = position.inMilliseconds / duration.inMilliseconds;
                buffered = buffer.inMilliseconds / duration.inMilliseconds;
              }

              return Column(
                children: [
                  Row(
                    children: [
                      Text(_formatDuration(position), style: const TextStyle(color: Colors.white)),
                      const Spacer(),
                      Text(_formatDuration(duration), style: const TextStyle(color: Colors.white)),
                    ],
                  ),
                  const SizedBox(height: 8),
                  GestureDetector(
                    onTapDown: (details) {
                      final box = context.findRenderObject() as RenderBox;
                      final width = box.size.width - 32; // padding
                      final tapPos = details.localPosition.dx;
                      final relative = tapPos / width;
                      final seekMs = relative * duration.inMilliseconds;
                      player.seek(Duration(milliseconds: seekMs.toInt()));
                    },
                    child: CustomProgressBar(
                      progress: progress,
                      buffered: buffered,
                      progressColor: Theme.of(context).colorScheme.secondary,
                    ),
                  ),
                ],
              );
            },
          ),
          const SizedBox(height: 16),
          // Buttons
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  StreamBuilder<bool>(
                    stream: player.stream.playing,
                    builder: (context, snapshot) {
                      final isPlaying = snapshot.data ?? false;
                      return IconButton(
                        icon: Icon(isPlaying ? Icons.pause : Icons.play_arrow, color: Colors.white),
                        onPressed: player.playOrPause,
                      );
                    },
                  ),
                  // Volume, Speed, etc.
                ],
              ),
              Row(
                children: [
                  IconButton(
                    icon: const Icon(Icons.picture_in_picture_alt, color: Colors.white),
                    onPressed: _toggleSmallWindow,
                    tooltip: "小窗模式",
                  ),
                  IconButton(
                    icon: const Icon(Icons.settings, color: Colors.white),
                    onPressed: () => setState(() => _isSettingsOpen = !_isSettingsOpen),
                  ),
                  IconButton(
                    icon: const Icon(Icons.fullscreen, color: Colors.white),
                    onPressed: _toggleFullScreen,
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSmallWindowUI() {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          Video(controller: controller, controls: NoVideoControls),
          // Simple overlay for drag and close
          Positioned.fill(
            child: GestureDetector(
              onPanUpdate: (details) async {
                await windowManager.startDragging();
              },
              child: Container(color: Colors.transparent),
            ),
          ),
          Positioned(
            top: 8,
            right: 8,
            child: IconButton(
              icon: const Icon(Icons.close, color: Colors.white),
              onPressed: () => exit(0),
            ),
          ),
          Positioned(
            bottom: 8,
            right: 8,
            child: IconButton(
              icon: const Icon(Icons.open_in_full, color: Colors.white),
              onPressed: _toggleSmallWindow,
            ),
          ),
        ],
      ),
    );
  }

  void _showIntroOutroDialog() {
    showDialog(
      context: context,
      barrierColor: Colors.transparent,
      builder: (ctx) => Center(
        child: IntroOutroDialog(
          duration: player.state.duration,
          currentPosition: player.state.position,
          initialIntroEndMs: 0, // Load from provider
          initialOutroStartMs: 0, // Load from provider
          onSave: (intro, outro) {
            context.read<IntroOutroProvider>().saveIntroOutro("season_id_placeholder", intro, outro);
            Navigator.pop(ctx);
          },
          onReset: () {
            // Reset logic
            Navigator.pop(ctx);
          },
        ),
      ),
    );
  }

  String _formatDuration(Duration duration) {
    String twoDigits(int n) => n.toString().padLeft(2, "0");
    String twoDigitMinutes = twoDigits(duration.inMinutes.remainder(60));
    String twoDigitSeconds = twoDigits(duration.inSeconds.remainder(60));
    return "${twoDigits(duration.inHours)}:$twoDigitMinutes:$twoDigitSeconds";
  }
}
