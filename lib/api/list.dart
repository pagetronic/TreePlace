import 'dart:math' as Math;

import 'package:agroneo_treeplace/api/api.dart';
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ListApi extends StatefulWidget {
  ListApiState state;

  ListApi({Key key}) : super(key: key);

  ListApi get(String url, Widget Function(dynamic) builder, {Key key}) {
    this.state = new ListApiState(builder, (paging) {
      var params = new Map<String, String>();
      if (paging != null) {
        params.addAll({"paging": paging});
      }
      ApiRequest.get(url, params: params, success: (json) {
        state.update(json);
      });
    });
    return this;
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
    setState(() {
      paging = json['paging']['next'];
      result.addAll(json['result']);
    });
  }
}
