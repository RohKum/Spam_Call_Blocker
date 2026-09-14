package com.dev2drop.cleanring.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RuleDao_Impl implements RuleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RuleEntity> __insertionAdapterOfRuleEntity;

  private final EntityDeletionOrUpdateAdapter<RuleEntity> __updateAdapterOfRuleEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRule;

  private final SharedSQLiteStatement __preparedStmtOfToggleRuleStatus;

  private final SharedSQLiteStatement __preparedStmtOfIncrementBlockCount;

  private final SharedSQLiteStatement __preparedStmtOfIncrementBlockCountByPattern;

  public RuleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRuleEntity = new EntityInsertionAdapter<RuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `rules` (`_id`,`pattern`,`type`,`action`,`is_enabled`,`label`,`created_at`,`blocked_count`,`expiry_timestamp`,`schedule_days`,`schedule_start_hour`,`schedule_end_hour`,`country_code`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPattern());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getAction());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getLabel());
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getBlockedCount());
        statement.bindLong(9, entity.getExpiryTimestamp());
        statement.bindString(10, entity.getScheduleDays());
        statement.bindLong(11, entity.getScheduleStartHour());
        statement.bindLong(12, entity.getScheduleEndHour());
        statement.bindString(13, entity.getCountryCode());
      }
    };
    this.__updateAdapterOfRuleEntity = new EntityDeletionOrUpdateAdapter<RuleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `rules` SET `_id` = ?,`pattern` = ?,`type` = ?,`action` = ?,`is_enabled` = ?,`label` = ?,`created_at` = ?,`blocked_count` = ?,`expiry_timestamp` = ?,`schedule_days` = ?,`schedule_start_hour` = ?,`schedule_end_hour` = ?,`country_code` = ? WHERE `_id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RuleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getPattern());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getAction());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getLabel());
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getBlockedCount());
        statement.bindLong(9, entity.getExpiryTimestamp());
        statement.bindString(10, entity.getScheduleDays());
        statement.bindLong(11, entity.getScheduleStartHour());
        statement.bindLong(12, entity.getScheduleEndHour());
        statement.bindString(13, entity.getCountryCode());
        statement.bindLong(14, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteRule = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rules WHERE _id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfToggleRuleStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE rules SET is_enabled = ? WHERE _id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementBlockCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE rules SET blocked_count = blocked_count + 1 WHERE _id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementBlockCountByPattern = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE rules SET blocked_count = blocked_count + 1 WHERE pattern = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRule(final RuleEntity rule, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRuleEntity.insertAndReturnId(rule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRule(final RuleEntity rule, final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        int _total = 0;
        __db.beginTransaction();
        try {
          _total += __updateAdapterOfRuleEntity.handle(rule);
          __db.setTransactionSuccessful();
          return _total;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRule(final long id, final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRule.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteRule.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object toggleRuleStatus(final long id, final boolean isEnabled,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfToggleRuleStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfToggleRuleStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementBlockCount(final long id,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementBlockCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementBlockCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementBlockCountByPattern(final String pattern,
      final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementBlockCountByPattern.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, pattern);
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementBlockCountByPattern.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RuleEntity>> getAllRulesFlow() {
    final String _sql = "SELECT * FROM rules ORDER BY _id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"rules"}, new Callable<List<RuleEntity>>() {
      @Override
      @NonNull
      public List<RuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfBlockedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "blocked_count");
          final int _cursorIndexOfExpiryTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_timestamp");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_days");
          final int _cursorIndexOfScheduleStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_start_hour");
          final int _cursorIndexOfScheduleEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_end_hour");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "country_code");
          final List<RuleEntity> _result = new ArrayList<RuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpBlockedCount;
            _tmpBlockedCount = _cursor.getInt(_cursorIndexOfBlockedCount);
            final long _tmpExpiryTimestamp;
            _tmpExpiryTimestamp = _cursor.getLong(_cursorIndexOfExpiryTimestamp);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpScheduleStartHour;
            _tmpScheduleStartHour = _cursor.getInt(_cursorIndexOfScheduleStartHour);
            final int _tmpScheduleEndHour;
            _tmpScheduleEndHour = _cursor.getInt(_cursorIndexOfScheduleEndHour);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            _item = new RuleEntity(_tmpId,_tmpPattern,_tmpType,_tmpAction,_tmpIsEnabled,_tmpLabel,_tmpCreatedAt,_tmpBlockedCount,_tmpExpiryTimestamp,_tmpScheduleDays,_tmpScheduleStartHour,_tmpScheduleEndHour,_tmpCountryCode);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllRulesSync(final Continuation<? super List<RuleEntity>> $completion) {
    final String _sql = "SELECT * FROM rules";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RuleEntity>>() {
      @Override
      @NonNull
      public List<RuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfBlockedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "blocked_count");
          final int _cursorIndexOfExpiryTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_timestamp");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_days");
          final int _cursorIndexOfScheduleStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_start_hour");
          final int _cursorIndexOfScheduleEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_end_hour");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "country_code");
          final List<RuleEntity> _result = new ArrayList<RuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpBlockedCount;
            _tmpBlockedCount = _cursor.getInt(_cursorIndexOfBlockedCount);
            final long _tmpExpiryTimestamp;
            _tmpExpiryTimestamp = _cursor.getLong(_cursorIndexOfExpiryTimestamp);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpScheduleStartHour;
            _tmpScheduleStartHour = _cursor.getInt(_cursorIndexOfScheduleStartHour);
            final int _tmpScheduleEndHour;
            _tmpScheduleEndHour = _cursor.getInt(_cursorIndexOfScheduleEndHour);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            _item = new RuleEntity(_tmpId,_tmpPattern,_tmpType,_tmpAction,_tmpIsEnabled,_tmpLabel,_tmpCreatedAt,_tmpBlockedCount,_tmpExpiryTimestamp,_tmpScheduleDays,_tmpScheduleStartHour,_tmpScheduleEndHour,_tmpCountryCode);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getActiveRulesSync(final Continuation<? super List<RuleEntity>> $completion) {
    final String _sql = "SELECT * FROM rules WHERE is_enabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RuleEntity>>() {
      @Override
      @NonNull
      public List<RuleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfBlockedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "blocked_count");
          final int _cursorIndexOfExpiryTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_timestamp");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_days");
          final int _cursorIndexOfScheduleStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_start_hour");
          final int _cursorIndexOfScheduleEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_end_hour");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "country_code");
          final List<RuleEntity> _result = new ArrayList<RuleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RuleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpBlockedCount;
            _tmpBlockedCount = _cursor.getInt(_cursorIndexOfBlockedCount);
            final long _tmpExpiryTimestamp;
            _tmpExpiryTimestamp = _cursor.getLong(_cursorIndexOfExpiryTimestamp);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpScheduleStartHour;
            _tmpScheduleStartHour = _cursor.getInt(_cursorIndexOfScheduleStartHour);
            final int _tmpScheduleEndHour;
            _tmpScheduleEndHour = _cursor.getInt(_cursorIndexOfScheduleEndHour);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            _item = new RuleEntity(_tmpId,_tmpPattern,_tmpType,_tmpAction,_tmpIsEnabled,_tmpLabel,_tmpCreatedAt,_tmpBlockedCount,_tmpExpiryTimestamp,_tmpScheduleDays,_tmpScheduleStartHour,_tmpScheduleEndHour,_tmpCountryCode);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRuleById(final long id, final Continuation<? super RuleEntity> $completion) {
    final String _sql = "SELECT * FROM rules WHERE _id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RuleEntity>() {
      @Override
      @Nullable
      public RuleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "_id");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "is_enabled");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at");
          final int _cursorIndexOfBlockedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "blocked_count");
          final int _cursorIndexOfExpiryTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "expiry_timestamp");
          final int _cursorIndexOfScheduleDays = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_days");
          final int _cursorIndexOfScheduleStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_start_hour");
          final int _cursorIndexOfScheduleEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "schedule_end_hour");
          final int _cursorIndexOfCountryCode = CursorUtil.getColumnIndexOrThrow(_cursor, "country_code");
          final RuleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpBlockedCount;
            _tmpBlockedCount = _cursor.getInt(_cursorIndexOfBlockedCount);
            final long _tmpExpiryTimestamp;
            _tmpExpiryTimestamp = _cursor.getLong(_cursorIndexOfExpiryTimestamp);
            final String _tmpScheduleDays;
            _tmpScheduleDays = _cursor.getString(_cursorIndexOfScheduleDays);
            final int _tmpScheduleStartHour;
            _tmpScheduleStartHour = _cursor.getInt(_cursorIndexOfScheduleStartHour);
            final int _tmpScheduleEndHour;
            _tmpScheduleEndHour = _cursor.getInt(_cursorIndexOfScheduleEndHour);
            final String _tmpCountryCode;
            _tmpCountryCode = _cursor.getString(_cursorIndexOfCountryCode);
            _result = new RuleEntity(_tmpId,_tmpPattern,_tmpType,_tmpAction,_tmpIsEnabled,_tmpLabel,_tmpCreatedAt,_tmpBlockedCount,_tmpExpiryTimestamp,_tmpScheduleDays,_tmpScheduleStartHour,_tmpScheduleEndHour,_tmpCountryCode);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
