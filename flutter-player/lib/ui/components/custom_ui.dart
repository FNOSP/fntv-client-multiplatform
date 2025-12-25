import 'package:flutter/material.dart';

class CustomProgressBar extends StatelessWidget {
  final double progress; // 0.0 to 1.0
  final double buffered; // 0.0 to 1.0
  final double height;
  final Color backgroundColor;
  final Color bufferColor;
  final Color progressColor;

  const CustomProgressBar({
    super.key,
    required this.progress,
    required this.buffered,
    this.height = 4.0,
    this.backgroundColor = Colors.grey,
    this.bufferColor = Colors.white54, // Semi-transparent white as requested
    this.progressColor = Colors.blue,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: height,
      child: Stack(
        children: [
          // Background
          Container(
            color: backgroundColor.withOpacity(0.3),
          ),
          // Buffered
          FractionallySizedBox(
            widthFactor: buffered.clamp(0.0, 1.0),
            child: Container(
              color: bufferColor,
            ),
          ),
          // Progress
          FractionallySizedBox(
            widthFactor: progress.clamp(0.0, 1.0),
            child: Container(
              color: progressColor,
            ),
          ),
        ],
      ),
    );
  }
}

class CircularLoadingIndicator extends StatefulWidget {
  final double size;
  final Color color;

  const CircularLoadingIndicator({
    super.key,
    this.size = 48.0,
    this.color = Colors.white,
  });

  @override
  State<CircularLoadingIndicator> createState() => _CircularLoadingIndicatorState();
}

class _CircularLoadingIndicatorState extends State<CircularLoadingIndicator> with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 1),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return RotationTransition(
      turns: _controller,
      child: SizedBox(
        width: widget.size,
        height: widget.size,
        child: CustomPaint(
          painter: _RingPainter(color: widget.color),
        ),
      ),
    );
  }
}

class _RingPainter extends CustomPainter {
  final Color color;

  _RingPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 4.0
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final center = Offset(size.width / 2, size.height / 2);
    final radius = (size.width - 4.0) / 2;

    // Draw a partial arc (ring with gap)
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      0.0,
      5.0, // Almost full circle but not quite, to show rotation
      false,
      paint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class CustomToast extends StatelessWidget {
  final String message;
  final IconData? icon;

  const CustomToast({super.key, required this.message, this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.8),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, color: Colors.white, size: 20),
            const SizedBox(width: 8),
          ],
          Text(
            message,
            style: const TextStyle(color: Colors.white, fontSize: 14),
          ),
        ],
      ),
    );
  }
}
