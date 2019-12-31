import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ThreadView extends StatefulWidget {
  final String title;

  ThreadView({Key key, this.title}) : super(key: key);

  @override
  ThreadViewState createState() => ThreadViewState();
}

class ThreadViewState extends BaseState<ThreadView> {
  @override
  Widget build(BuildContext context) {
    final dynamic args = ModalRoute.of(context).settings.arguments;

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
              args['id'],
              style: Theme.of(context).textTheme.display1,
            ),
          ],
        ),
      ),
    );
  }
}
