import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/services.dart';
import 'database_helper.dart';

final prefixListProvider = StateNotifierProvider<PrefixNotifier, List<String>>((ref) {
  return PrefixNotifier();
});

final defaultRoleProvider = StateNotifierProvider<DefaultRoleNotifier, bool>((ref) {
  return DefaultRoleNotifier();
});

class DefaultRoleNotifier extends StateNotifier<bool> {
  DefaultRoleNotifier() : super(false) {
    checkStatus();
  }

  static const platform = MethodChannel('com.example.spamblocker/prefs');

  Future<void> checkStatus() async {
    try {
      final bool isDefault = await platform.invokeMethod('isDefaultApp');
      state = isDefault;
    } on PlatformException {
      state = false;
    }
  }

  Future<void> requestRole() async {
    try {
      final bool isDefault = await platform.invokeMethod('requestDefaultApp');
      state = isDefault;
    } on PlatformException {
      state = false;
    }
  }
}

class PrefixNotifier extends StateNotifier<List<String>> {
  PrefixNotifier() : super([]) {
    _loadPrefixes();
  }

  static const platform = MethodChannel('com.example.spamblocker/prefs');

  Future<void> _loadPrefixes() async {
    final prefixes = await DatabaseHelper.instance.queryAllPrefixes();
    state = prefixes;
    _syncWithNative(prefixes);
  }

  Future<void> addPrefix(String prefix) async {
    if (prefix.isEmpty || state.contains(prefix)) return;
    await DatabaseHelper.instance.insert(prefix);
    state = [...state, prefix];
    _syncWithNative(state);
  }

  Future<void> removePrefix(String prefix) async {
    await DatabaseHelper.instance.delete(prefix);
    state = state.where((p) => p != prefix).toList();
    _syncWithNative(state);
  }

  Future<void> _syncWithNative(List<String> prefixes) async {
    try {
      final String prefixesString = prefixes.join(',');
      await platform.invokeMethod('updatePrefixes', {'prefixes': prefixesString});
    } on PlatformException catch (e) {
      print("Failed to sync prefixes with native: '\${e.message}'.");
    }
  }
}
