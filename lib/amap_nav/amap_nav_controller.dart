import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../common/coordinate.dart';

const _navChannelPrefix = 'plugin/amap/nav';

typedef void NavCloseHandler();
typedef void NavMoreHandler();
typedef void NavCallbackHandler();
typedef void NavEndHandler(int type);

class AMapNavController {
  final MethodChannel _navChannel;
  final NavCloseHandler? onCloseHandler;
  final NavMoreHandler? onMoreHandler;
  final MethodChannel _componentChannel;
  final NavCallbackHandler? onInitNaviSuccess;
  final NavCallbackHandler? onReCalculateRoute;
  final NavCallbackHandler? onInitNaviFailure;
  final NavCallbackHandler? onNaviCancel;
  final NavEndHandler? onEndNavi;


  AMapNavController({
    required int viewId,
    this.onCloseHandler,
    this.onMoreHandler,
    this.onInitNaviSuccess,
    this.onEndNavi,
    this.onReCalculateRoute,
    this.onInitNaviFailure,
    this.onNaviCancel
  })  : _navChannel = MethodChannel('$_navChannelPrefix/$viewId'),
        _componentChannel = MethodChannel('$_navChannelPrefix');

  Future startAMapNav({required Map params}) {
    return _navChannel
        .invokeMethod('startNav', params)
        .then((onValue) {
            return onValue;
        });
  }

  Future playTTS(String tts) {
    return _navChannel.invokeMethod('playTTS', {"content":tts});
  }

  Future changeSpeekType(int speakType) {
    return _navChannel.invokeMethod('speekType', {"speekType":"${speakType}"});
  }

  Future changeNaviMode(int mode) {
    return _navChannel.invokeMethod('changeNaviMode', {"mode":"${mode}"});
  }

  Future changeDayNight(int daynight) {
    return _navChannel.invokeMethod('changeDayNight', {"daynight":"${daynight}"});
  }

  Future stopNav() {
    return _navChannel.invokeMethod('stopNav');
  }

  Future startComponent({
    required Coordinate coordinate,
  }) {
    return _componentChannel
        .invokeMethod('startComponentNav', coordinate.toJsonString())
        .then((onValue) {
      return onValue;
    });
  }

  void initNavChannel(BuildContext context) {
    _navChannel.setMethodCallHandler((handler) async{
      switch (handler.method) {
        case 'close_nav':
          if (onCloseHandler != null) {
            onCloseHandler!();
          } else {
            Navigator.pop(context);
          }
          break;
        case 'more_nav':
          if (onMoreHandler != null) {
            onMoreHandler!();
          }
          break;
        case 'onInitNaviSuccess':
          if (onInitNaviSuccess != null) {
            onInitNaviSuccess!();
          }
          break;
        case 'onInitNaviFailure':
          if (onInitNaviSuccess != null) {
            onInitNaviFailure!();
          }
          break;
        case 'onReCalculateRoute':
          if (onReCalculateRoute != null) {
            onReCalculateRoute!();
          }
          break;
        case 'onCalculateRouteFailure':
            print('ssssss');
          break;
        case 'onEndNavi':
          if (onEndNavi != null) {
            Map args = handler.arguments;
            onEndNavi!(args['type']);
          }
          break;
        case 'onNaviCancel':
          if (onNaviCancel != null) {
            onNaviCancel!();
          }
          break;
        default:
      }
    });
  }

  void dispose() {}
}
