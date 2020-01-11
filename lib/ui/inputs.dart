import 'package:flutter/material.dart';

class TextObjects {
  static TextField getTextarea() {
    return TextField(
      minLines: 3,
      maxLines: 10000,
      toolbarOptions:
          ToolbarOptions(copy: true, cut: true, paste: true, selectAll: true),
    );
  }
}
