import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class DatabaseHelper {
  static const _databaseName = "SpamBlocker.db";
  static const _databaseVersion = 1;

  static const table = 'prefixes';

  static const columnId = '_id';
  static const columnPrefix = 'prefix';

  // make this a singleton class
  DatabaseHelper._privateConstructor();
  static final DatabaseHelper instance = DatabaseHelper._privateConstructor();

  static Database? _database;
  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  _initDatabase() async {
    final documentsDirectory = await getApplicationDocumentsDirectory();
    final path = join(documentsDirectory.path, _databaseName);
    return await openDatabase(path,
        version: _databaseVersion, onCreate: _onCreate);
  }

  Future _onCreate(Database db, int version) async {
    await db.execute('''
          CREATE TABLE $table (
            $columnId INTEGER PRIMARY KEY,
            $columnPrefix TEXT NOT NULL UNIQUE
          )
          ''');
  }

  Future<int> insert(String prefix) async {
    Database db = await instance.database;
    return await db.insert(table, {columnPrefix: prefix}, conflictAlgorithm: ConflictAlgorithm.ignore);
  }

  Future<List<String>> queryAllPrefixes() async {
    Database db = await instance.database;
    final List<Map<String, dynamic>> maps = await db.query(table);
    return List.generate(maps.length, (i) {
      return maps[i][columnPrefix] as String;
    });
  }

  Future<int> delete(String prefix) async {
    Database db = await instance.database;
    return await db.delete(table, where: '$columnPrefix = ?', whereArgs: [prefix]);
  }
}
