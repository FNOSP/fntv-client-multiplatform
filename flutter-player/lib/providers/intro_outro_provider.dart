import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

class IntroOutroInfo {
  final int introEndMs;
  final int outroStartMs;

  IntroOutroInfo({required this.introEndMs, required this.outroStartMs});

  Map<String, dynamic> toJson() => {
        'introEndMs': introEndMs,
        'outroStartMs': outroStartMs,
      };

  factory IntroOutroInfo.fromJson(Map<String, dynamic> json) {
    return IntroOutroInfo(
      introEndMs: json['introEndMs'] ?? 0,
      outroStartMs: json['outroStartMs'] ?? 0,
    );
  }
}

class IntroOutroProvider with ChangeNotifier {
  final Map<String, IntroOutroInfo> _cache = {};
  
  // Assuming seasonId is part of the context when playing an episode
  // If we don't have explicit seasonId, we might need to rely on the parentGuid of the episode item
  Future<void> saveIntroOutro(String seasonId, int introEndMs, int outroStartMs) async {
    final info = IntroOutroInfo(introEndMs: introEndMs, outroStartMs: outroStartMs);
    _cache[seasonId] = info;
    
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('intro_outro_$seasonId', jsonEncode(info.toJson()));
    notifyListeners();
  }

  Future<IntroOutroInfo?> getIntroOutro(String seasonId) async {
    if (_cache.containsKey(seasonId)) {
      return _cache[seasonId];
    }

    final prefs = await SharedPreferences.getInstance();
    final jsonStr = prefs.getString('intro_outro_$seasonId');
    if (jsonStr != null) {
      final info = IntroOutroInfo.fromJson(jsonDecode(jsonStr));
      _cache[seasonId] = info;
      return info;
    }
    return null;
  }
  
  Future<void> resetIntroOutro(String seasonId) async {
    _cache.remove(seasonId);
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('intro_outro_$seasonId');
    notifyListeners();
  }
}
