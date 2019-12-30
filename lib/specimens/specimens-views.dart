import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import '../settings.dart' as settings;
import '../ui/widgets.dart';

class SpecimensViews extends StatefulWidget {
  final String title;

  SpecimensViews({Key key, this.title}) : super(key: key);

  @override
  _SpecimensViewsState createState() => _SpecimensViewsState();
}

class _SpecimensViewsState extends BaseState<SpecimensViews> {
  @override
  Widget build(BuildContext context) {
    try {
      return Scaffold(
        appBar: AppBar(
          title: Text(widget.title),
          actions: <Widget>[connector],
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text(
                'list',
                style: Theme.of(context).textTheme.display1,
              ),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            Navigator.pushNamed(context, '/specimens/create');
          },
          backgroundColor: settings.secondaryColor,
          tooltip: 'Increment',
          child: Icon(Icons.add),
        ),
      );
    } finally {}
  }
}
