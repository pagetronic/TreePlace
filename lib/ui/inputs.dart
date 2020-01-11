import 'package:flutter/material.dart';

// Define a custom Form widget.
class TextObjects extends StatefulWidget {
  TextObjectsState state;

  @override
  TextObjectsState createState() {
    state = TextObjectsState();
    return state;
  }

  String text() {
    return state.controller.text;
  }
}

class TextObjectsState extends State<TextObjects> {
  final controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    TextField area = TextField(
      controller: controller,
      minLines: 3,
      maxLines: 10000,
      toolbarOptions:
          ToolbarOptions(copy: true, cut: true, paste: true, selectAll: true),
    );

    var obj = Column(children: <Widget>[
      area,
      SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: <Widget>[
              RaisedButton(
                padding: const EdgeInsets.all(0.0),
                child: new Text('link',
                    style: TextStyle(decoration: TextDecoration.underline)),
                onPressed: null,
              ),
              RaisedButton(
                padding: const EdgeInsets.all(0.0),
                child: new Text('doc'),
                onPressed: null,
              ),
              RaisedButton(
                padding: const EdgeInsets.all(0.0),
                child: new Text('bold',
                    style: TextStyle(fontWeight: FontWeight.bold)),
                onPressed: null,
              ),
              RaisedButton(
                padding: const EdgeInsets.all(0.0),
                child: new Text('italic',
                    style: TextStyle(fontStyle: FontStyle.italic)),
                onPressed: null,
              ),
              RaisedButton(
                padding: const EdgeInsets.all(0.0),
                child: new Text('quote'),
                onPressed: null,
              )
            ],
          ))
    ]);
    return obj;
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }
}
