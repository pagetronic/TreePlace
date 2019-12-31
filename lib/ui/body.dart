import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class BodyViews extends StatefulWidget {
  final String title;
  final List<Widget> children;

  BodyViews({Key key, this.children, this.title}) : super(key: key);

  @override
  BodyState createState() => BodyState();
}

class BodyState extends BaseState<BodyViews> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text(widget.title),
          actions: <Widget>[connector],
        ),
        body: DefaultTabController(
            initialIndex: 0,
            length: widget.children.length,
            child: Scaffold(
                body: TabBarView(
              children: widget.children,
            ))));
  }
}
