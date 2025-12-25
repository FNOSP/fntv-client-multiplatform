import 'package:flutter/material.dart';

class SettingsMenu extends StatefulWidget {
  final bool isTvEpisode;
  final String? currentAspectRatio;
  final String? currentWindowRatio;
  final Function(String) onAspectRatioChanged;
  final Function(String) onWindowRatioChanged;
  final VoidCallback onIntroOutroTap;
  final VoidCallback onClose;

  const SettingsMenu({
    super.key,
    this.isTvEpisode = false,
    this.currentAspectRatio,
    this.currentWindowRatio,
    required this.onAspectRatioChanged,
    required this.onWindowRatioChanged,
    required this.onIntroOutroTap,
    required this.onClose,
  });

  @override
  State<SettingsMenu> createState() => _SettingsMenuState();
}

class _SettingsMenuState extends State<SettingsMenu> {
  String? _subMenu; // null (main), 'aspect_ratio', 'window_ratio'

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 300,
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.9),
        borderRadius: BorderRadius.circular(12),
      ),
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: AnimatedSwitcher(
        duration: const Duration(milliseconds: 200),
        child: _buildCurrentMenu(),
      ),
    );
  }

  Widget _buildCurrentMenu() {
    switch (_subMenu) {
      case 'aspect_ratio':
        return _buildSelectionMenu(
          title: '画面比例',
          options: ['默认', '4:3', '16:9', '21:9'],
          current: widget.currentAspectRatio ?? '默认',
          onSelect: (val) {
            widget.onAspectRatioChanged(val);
            setState(() => _subMenu = null);
          },
          onBack: () => setState(() => _subMenu = null),
        );
      case 'window_ratio':
        return _buildSelectionMenu(
          title: '窗口比例',
          options: ['自动', '4:3', '16:9', '21:9'],
          current: widget.currentWindowRatio ?? '自动',
          onSelect: (val) {
            widget.onWindowRatioChanged(val);
            setState(() => _subMenu = null);
          },
          onBack: () => setState(() => _subMenu = null),
        );
      default:
        return _buildMainMenu();
    }
  }

  Widget _buildMainMenu() {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('设置', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
              GestureDetector(
                onTap: () {}, // Advanced settings if needed
                child: const Row(
                  children: [
                    Text('高级', style: TextStyle(color: Colors.white70, fontSize: 12)),
                    Icon(Icons.chevron_right, color: Colors.white70, size: 16),
                  ],
                ),
              ),
            ],
          ),
        ),
        const Divider(color: Colors.white24, height: 1),
        _buildSwitchItem('自动连播', true, (v) {}), // Placeholder logic
        if (widget.isTvEpisode)
          _buildMenuItem(
            '跳过片头/片尾',
            '未设置',
            onTap: widget.onIntroOutroTap,
          ),
        _buildMenuItem(
          '画面比例',
          widget.currentAspectRatio ?? '默认',
          onTap: () => setState(() => _subMenu = 'aspect_ratio'),
        ),
        _buildMenuItem(
          '窗口比例',
          widget.currentWindowRatio ?? '自动',
          onTap: () => setState(() => _subMenu = 'window_ratio'),
        ),
        // Add Audio track logic here if needed
      ],
    );
  }

  Widget _buildMenuItem(String title, String value, {required VoidCallback onTap}) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(title, style: const TextStyle(color: Colors.white70)),
            Row(
              children: [
                Text(value, style: const TextStyle(color: Colors.white, fontSize: 12)),
                const Icon(Icons.chevron_right, color: Colors.white70, size: 16),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchItem(String title, bool value, Function(bool) onChanged) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(title, style: const TextStyle(color: Colors.white70)),
          Switch(
            value: value,
            onChanged: onChanged,
            activeColor: Colors.blue,
            activeTrackColor: Colors.blue.withOpacity(0.5),
          ),
        ],
      ),
    );
  }

  Widget _buildSelectionMenu({
    required String title,
    required List<String> options,
    required String current,
    required Function(String) onSelect,
    required VoidCallback onBack,
  }) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Row(
            children: [
              GestureDetector(
                onTap: onBack,
                child: const Icon(Icons.chevron_left, color: Colors.white, size: 20),
              ),
              const SizedBox(width: 8),
              Text(title, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
            ],
          ),
        ),
        const Divider(color: Colors.white24, height: 1),
        ...options.map((opt) {
          final isSelected = opt == current;
          return InkWell(
            onTap: () => onSelect(opt),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(opt, style: TextStyle(color: isSelected ? Colors.blue : Colors.white70)),
                  if (isSelected) const Icon(Icons.check, color: Colors.blue, size: 16),
                ],
              ),
            ),
          );
        }),
      ],
    );
  }
}

class IntroOutroDialog extends StatefulWidget {
  final Duration duration;
  final Duration currentPosition;
  final int initialIntroEndMs;
  final int initialOutroStartMs;
  final Function(int, int) onSave;
  final VoidCallback onReset;

  const IntroOutroDialog({
    super.key,
    required this.duration,
    required this.currentPosition,
    required this.initialIntroEndMs,
    required this.initialOutroStartMs,
    required this.onSave,
    required this.onReset,
  });

  @override
  State<IntroOutroDialog> createState() => _IntroOutroDialogState();
}

class _IntroOutroDialogState extends State<IntroOutroDialog> {
  late double _introEnd;
  late double _outroStart;

  @override
  void initState() {
    super.initState();
    _introEnd = widget.initialIntroEndMs.toDouble();
    _outroStart = widget.initialOutroStartMs > 0
        ? widget.initialOutroStartMs.toDouble()
        : widget.duration.inMilliseconds.toDouble();
  }

  String _formatDuration(double ms) {
    final duration = Duration(milliseconds: ms.toInt());
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    final minutes = twoDigits(duration.inMinutes.remainder(60));
    final seconds = twoDigits(duration.inSeconds.remainder(60));
    return "$minutes:$seconds";
  }

  @override
  Widget build(BuildContext context) {
    final maxMs = widget.duration.inMilliseconds.toDouble();

    return Container(
      width: 350,
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.9),
        borderRadius: BorderRadius.circular(12),
      ),
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text('跳过片头/片尾', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
              TextButton(
                onPressed: widget.onReset,
                child: const Text('重置', style: TextStyle(color: Colors.white38)),
              ),
            ],
          ),
          const Text('生效范围：《...》第 1 季', style: TextStyle(color: Colors.white38, fontSize: 12)),
          const Divider(color: Colors.white24, height: 24),

          // Intro Slider
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Text('片头时长', style: TextStyle(color: Colors.white)),
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(color: Colors.white10, borderRadius: BorderRadius.circular(4)),
                    child: Text(_formatDuration(_introEnd), style: const TextStyle(color: Colors.white)),
                  ),
                ],
              ),
              GestureDetector(
                onTap: () {
                  setState(() {
                    _introEnd = widget.currentPosition.inMilliseconds.toDouble();
                    widget.onSave(_introEnd.toInt(), _outroStart.toInt());
                  });
                },
                child: Text('将当前时间 ${_formatDuration(widget.currentPosition.inMilliseconds.toDouble())} 设为片头',
                    style: const TextStyle(color: Colors.blue, fontSize: 12)),
              ),
            ],
          ),
          SliderTheme(
            data: SliderTheme.of(context).copyWith(
              activeTrackColor: Colors.blue,
              thumbColor: Colors.white,
              trackHeight: 2,
            ),
            child: Slider(
              value: _introEnd.clamp(0.0, maxMs),
              min: 0.0,
              max: maxMs / 2, // Limit intro to first half
              onChanged: (val) {
                setState(() => _introEnd = val);
              },
              onChangeEnd: (val) => widget.onSave(val.toInt(), _outroStart.toInt()),
            ),
          ),
          const Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('开始', style: TextStyle(color: Colors.white38, fontSize: 10)),
              Text('10 分钟', style: TextStyle(color: Colors.white38, fontSize: 10)), // Dynamic max
            ],
          ),

          const SizedBox(height: 16),

          // Outro Slider
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Text('片尾时长', style: TextStyle(color: Colors.white)),
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(color: Colors.white10, borderRadius: BorderRadius.circular(4)),
                    child: Text(_formatDuration(maxMs - _outroStart), style: const TextStyle(color: Colors.white)),
                  ),
                ],
              ),
              GestureDetector(
                onTap: () {
                   setState(() {
                    _outroStart = widget.currentPosition.inMilliseconds.toDouble();
                    widget.onSave(_introEnd.toInt(), _outroStart.toInt());
                  });
                },
                child: Text('将当前剩余时长 ${_formatDuration(maxMs - widget.currentPosition.inMilliseconds.toDouble())} 设为片尾',
                    style: const TextStyle(color: Colors.blue, fontSize: 12)),
              ),
            ],
          ),
          SliderTheme(
            data: SliderTheme.of(context).copyWith(
              activeTrackColor: Colors.blue,
              thumbColor: Colors.white,
              trackHeight: 2,
            ),
            child: Slider(
              value: _outroStart.clamp(0.0, maxMs),
              min: maxMs / 2, // Limit outro to second half
              max: maxMs,
              onChanged: (val) {
                setState(() => _outroStart = val);
              },
              onChangeEnd: (val) => widget.onSave(_introEnd.toInt(), val.toInt()),
            ),
          ),
          const Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('10 分钟', style: TextStyle(color: Colors.white38, fontSize: 10)),
              Text('结束', style: TextStyle(color: Colors.white38, fontSize: 10)),
            ],
          ),
        ],
      ),
    );
  }
}
