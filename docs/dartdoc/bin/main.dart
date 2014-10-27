import 'package:connect_json_docs/connect_json_docs.dart';
import 'dart:io';

void main(List<String> args) {
  final schemaRootDir = args.first;
  final outputDir = args[1];
  createConnectDocs(new Directory(schemaRootDir), new Directory(outputDir))
      .then((_) => print('done'));
}