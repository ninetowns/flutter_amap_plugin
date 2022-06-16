import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../amap_location/amap_location_options.dart';
import '../common/coordinate.dart';

const _locChannelPrefix = 'plugin/amap/location';

typedef void LocationCallHandler(
    String address, double lon, double lat, Error error);

class AMapLocationController {
  final MethodChannel _locChannel;

  AMapLocationController() : _locChannel = MethodChannel(_locChannelPrefix);

  void initLocationChannel({
    required LocationCallHandler onLocationCallHandler,
  }) {
    _locChannel.setMethodCallHandler((handler) async{
      switch (handler.method) {
        case 'locationError':
          if (onLocationCallHandler != null) {
            onLocationCallHandler(
                '', 0, 0, FlutterError(handler.arguments));
          }
          break;
        case 'locationSuccess':
          print(handler.arguments);
          stopLocation();
          if (onLocationCallHandler != null) {
            onLocationCallHandler(
                '', handler.arguments['lon'], handler.arguments['lat'], FlutterError(''));
          }
          break;
        case 'reGeocodeSuccess':
          print(handler.arguments);
          stopLocation();
          if (onLocationCallHandler != null) {
            onLocationCallHandler(handler.arguments['address'],
                handler.arguments['lon'], handler.arguments['lat'], FlutterError(''));
          }
          break;
        default:
      }
    });
  }

  Future initLocation({
    required LocationCallHandler onLocationCallHandler,
  }) async {
    initLocationChannel(onLocationCallHandler: onLocationCallHandler);
    var result = await _locChannel.invokeMethod('initLocation');
    return result;
  }

  Future startSingleLocation({
    required AMapLocationOptions options,
  }) async {
    var result = await _locChannel.invokeMethod(
        'startSingleLocation', options.toJsonString());
    return result;
  }

  Future stopLocation() async {
    var result = await _locChannel.invokeMethod('stopLocation');
    return result;
  }
}
