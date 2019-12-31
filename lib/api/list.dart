import 'dart:math' as Math;

import 'package:agroneo_treeplace/api/api.dart';
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ListApi extends StatefulWidget {
  final ListApiState state;

  ListApi(this.state);

  static ListApi get(String url, Widget Function(dynamic) builder,
      {String key}) {
    ListApiState state;
    state = new ListApiState(builder, (paging) {
      var params = new Map<String, String>();
      if (paging != null) {
        params.addAll({"paging": paging});
      }
      ApiRequest.get(url, params: params, success: (json) {
        var result = (key != null) ? json[key] : json;
        state.update(result);
      });
    });
    return new ListApi(state);
  }

  @override
  ListApiState createState() {
    return state;
  }

}

class ListApiState extends BaseState<ListApi> {
  List<dynamic> result = [];
  String paging;

  Widget Function(dynamic) builder;
  Function(String) getNext;

  ListApiState(Widget Function(dynamic) builder, Function(String) getNext) {
    this.builder = builder;
    this.getNext = getNext;
    getNext(null);
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
        itemCount: result.length,
        itemBuilder: (context, index) {
          if (paging != null && index == Math.max(0, result.length - 1)) {
            getNext(paging);
            paging = null;
          }
          return builder(result[index]);
        });
  }

  void update(json) {
    if (!mounted) {
      paging = json['paging']['next'];
      result.addAll(json['result']);
      return;
    }
    setState(() {
      paging = json['paging']['next'];
      result.addAll(json['result']);
    });
  }
}
