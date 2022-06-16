import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_amap_plugin/common/coordinate.dart';

const _convertChannelPrefix = 'plugin/amap/search/convert';

typedef void ConvertCallHandler(
    Coordinate coordinate, String address, Error error);

typedef void SearchCallHandler(
    List items);

class AMapConvertController {
  final MethodChannel _convertChannel;

  AMapConvertController()
      : _convertChannel = MethodChannel(_convertChannelPrefix);

  void initSearchChannel({
    required SearchCallHandler onSearchCallHandler,
  }) {
    _convertChannel.setMethodCallHandler((handler) async {
      switch (handler.method) {
        case 'onSearchKeyword':
          onSearchCallHandler(handler.arguments);
          break;
      }
    });
  }


  void initConvertChannel({
    required ConvertCallHandler onConvertCallHandler,
  }) {
    _convertChannel.setMethodCallHandler((handler) async{
      switch (handler.method) {
        case 'onCoordinateToGeo':
          if (onConvertCallHandler != null) {
            String address = handler.arguments['address'].toString();
            onConvertCallHandler(Coordinate(0,0), address, Error());
          }
          break;
        case 'onGeoToCoordinate':
          if (onConvertCallHandler != null) {
            Coordinate coordinate = Coordinate(
                double.parse(handler.arguments['lat'].toString()),
                double.parse(handler.arguments['lon'].toString()));
            onConvertCallHandler(coordinate, '', Error());
          }
          break;
        case 'onConvertError':
          if (onConvertCallHandler != null) {
            onConvertCallHandler(
                Coordinate(0,0), '', FlutterError(handler.arguments.toString()));
          }
          break;
      }
    });
  }

  Future distance(Map params) async {
    var result = await _convertChannel.invokeMethod('distance', params);
    return result;
  }

  Future searchKeyword(String keyword,String city) async {
    var result = await _convertChannel.invokeMethod('searchKeyword', {'keyword':keyword,'city':city});
    return result;
  }

  Future geoConvertToCoordinate(String geo) async {
    var result = await _convertChannel.invokeMethod('geoToCoordinate', geo);
    return result;
  }

  Future coordinateConvertToGeo(Coordinate coordinate) async {
    var result = await _convertChannel.invokeMethod(
        'coordinateToGeo', coordinate.toJsonString());
    return result;
  }
}
