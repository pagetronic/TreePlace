import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import '../settings.dart' as settings;
import '../ui/widgets.dart';

class SpecimensPage extends StatefulWidget {
  final String title;

  SpecimensPage({Key key, this.title}) : super(key: key);

  @override
  _SpecimensPageState createState() => _SpecimensPageState();
}

class _SpecimensPageState extends BaseState<SpecimensPage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

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
                '$_counter',
                style: Theme.of(context).textTheme.display1,
              ),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: _incrementCounter,
          backgroundColor: settings.secondaryColor,
          tooltip: 'Increment',
          child: Icon(Icons.add),
        ),
      );
    } finally {}
  }
}
