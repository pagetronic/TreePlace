import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

import '../ui/widgets.dart';

class SpecimensCreate extends StatefulWidget {
  final String title;

  SpecimensCreate({Key key, this.title}) : super(key: key);

  @override
  _SpecimensCreateState createState() => _SpecimensCreateState();
}

class _SpecimensCreateState extends BaseState<SpecimensCreate> {
  @override
  Widget build(BuildContext context) {
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
              'creation',
              style: Theme.of(context).textTheme.display1,
            ),
          ],
        ),
      ),
    );
  }
}
