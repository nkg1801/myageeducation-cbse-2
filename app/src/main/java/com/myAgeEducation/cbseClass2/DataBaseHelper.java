package com.myAgeEducation.cbseClass2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.myAgeEducation.cbsecommon.Question;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

class DataBaseHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 3;
    private final String dbPath;
    private static final String DB_NAME = "questions_cbse2.db";
    private static final int CHAPTER_COLUMN = 1;
    private static final int CHAPTER_NAME_COLUMN = 2;
    private static final int QUESTION_TEXT_COLUMN = 3;
    private static final int OPTION_1_COLUMN = 4;
    private static final int OPTION_2_COLUMN = 5;
    private static final int OPTION_3_COLUMN = 6;
    private static final int OPTION_4_COLUMN = 7;
    private static final int ANSWER_COLUMN = 8;
    private static final int IMAGE_COLUMN = 9;
    private static final int SUPPORTIVE_TEXT_COLUMN = 10;

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    DataBaseHelper(Context context)
    {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        dbPath = context.getDatabasePath(DB_NAME).getPath();
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    void createDataBase() throws IOException
    {
        boolean dbExist = checkDataBase();
        boolean doCopy;

        doCopy = !dbExist;

        if(doCopy)
        {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.

            //this.getReadableDatabase();
            SQLiteDatabase database = this.getReadableDatabase();
            database.close();

            try
            {
                copyDataBase();
            }
            catch (IOException e)
            {
                throw new Error("Error copying database: " + e.getMessage());
            }
        }
        else
        {
            // this will trigger the onUpgrade method
            SQLiteDatabase database = this.getReadableDatabase();
            database.close();
        }
    }
    /**
        * Check if the database already exist to avoid re-copying the file each time you open the application.
        * @return true if it exists, false if it doesn't
     **/
    private boolean checkDataBase()
    {
        File databasePath = myContext.getDatabasePath(dbPath + DB_NAME);
        return databasePath.exists();
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException
    {
        Log.d("DB_COPY", "Starting database copy");
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = dbPath + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;

        while ((length = myInput.read(buffer)) > 0)
        {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        Log.d("DB_COPY", "Database copied successfully");
    }

    void openDataBase() throws SQLException {

        //Open the database
        String myPath = dbPath + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion)
        {
            try
            {
                copyDataBase();
            }
            catch (IOException e)
            {
                throw new Error("Error copying database: " + e.getMessage());
            }
        }
    }

    private boolean isAnswerColumnBlank(Cursor cursor)
    {
        if(cursor.isNull(8))
        {
            return true;
        }
        return cursor.getString(8).trim().isEmpty();
    }

    ArrayList<Question> getAllQuestions(String tableName, int set)
    {
        Cursor cursor;
        String query = "select * from " + tableName + " where questionSet = " + set + " order by chapter";
        try
        {
             cursor = myDataBase.rawQuery(query, null);
        }
        catch(Exception e)
        {
            Log.d("CBSE_GetAllQuestions1", e.getMessage());
            return null;
        }
        ArrayList<Question> records = new ArrayList<>();

        if(cursor.getCount() > 0)
        {
            if(cursor.moveToFirst())
            {
                do
                {
                    Question q = new Question();
                    if(isAnswerColumnBlank(cursor))
                    {
                        continue; //answer column is blank, just skip this row
                    }

                    q.setChapter(cursor.getInt(CHAPTER_COLUMN));
                    q.setChapterName(cursor.getString(CHAPTER_NAME_COLUMN));
                    q.setQuestion(cursor.getString(QUESTION_TEXT_COLUMN));
                    q.setOption1(cursor.getString(OPTION_1_COLUMN));
                    q.setOption2(cursor.getString(OPTION_2_COLUMN));
                    q.setOption3(cursor.getString(OPTION_3_COLUMN));
                    q.setOption4(cursor.getString(OPTION_4_COLUMN));
                    q.setAnswer(cursor.getString(ANSWER_COLUMN));
                    q.setImage(cursor.getString(IMAGE_COLUMN));
                    q.setSupportiveText(cursor.getString(SUPPORTIVE_TEXT_COLUMN));
                    records.add(q);

                }while (cursor.moveToNext());
            }
        }
        cursor.close();
        return records;
    }

    boolean addQuestions(String tableName, ArrayList<Question> questionList, int set)
    {

        tableName = tableName.toUpperCase();

        deleteTableData(tableName, set);// lets delete existing questions for this set just to make sure previous things are not there

		for(int i = 0; i < questionList.size(); i++) {
            Question question = questionList.get(i);

			ContentValues columnValues = new ContentValues();
            columnValues.put("chapter", question.getChapter());
            columnValues.put("chapterName", question.getChapterName());
            columnValues.put("question", question.getQuestion());
            columnValues.put("option1", question.getOption1());
            columnValues.put("option2", question.getOption2());
            columnValues.put("option3", question.getOption3());
            columnValues.put("option4", question.getOption4());
            columnValues.put("answer", question.getAnswer());
            columnValues.put("image", question.getImage());
            columnValues.put("supportiveText", question.getSupportiveText());
            columnValues.put("questionSet", set);

			try {
                myDataBase.insert(tableName, null, columnValues);
            }
            catch(Exception e)
            {
                Log.d("InsertError", e.getMessage());
                return false;
            }
        }
        return true;
    }

    void updateDownloadStatus(String subject, int set)
    {
        try {
            String tableName = "DOWNLOAD_STATUS_" + subject.toUpperCase();
            String columnName = "SET" + set;
            //update DOWNLOAD_STATUS_MATHS set SET1=1
            String query = "update " + tableName + " set " + columnName + " = 1";
            Log.d("QueryString", query);
            myDataBase.execSQL(query);
        }
        catch(Exception e)
        {
            Log.d("ERROR", e.getMessage());
        }
    }

    void resetDownloadStatus(String subject)
    {
        try {
            String tableName = "DOWNLOAD_STATUS_" + subject.toUpperCase();
            for (int set = Util.FIRST_SET; set <= Util.LAST_SET; set++) {
                String columnName = "SET" + String.valueOf(set);
                //update DOWNLOAD_STATUS_MATHS set SET1=1
                String query = "update " + tableName + " set " + columnName + " = 0";
                myDataBase.execSQL(query);
            }
        }
        catch(Exception e)
        {
            Log.d("ERROR", e.getMessage());
        }
    }

    void updateLocalQuestionDatabaseVersionInfo(String subject, int version)
    {
        try {
            String tableName = "QUESTION_DATABASE_VERSION";
            String columnName = subject;
            //update DOWNLOAD_STATUS_MATHS set SET1=1
            String query = "update " + tableName + " set " + columnName + "=" + version;
            Log.d("UpdateVersionInfoQuery", query);
            myDataBase.execSQL(query);
        }
        catch(Exception e)
        {
            Log.d("ERROR", e.getMessage());
        }
    }

    void deleteTableData(String tableName, int setNumber)
    {
        String query = "delete from " + tableName + " where questionSet = " + setNumber;
        try
        {
            myDataBase.execSQL(query);
            Log.d("DELETE", "DELETE Successful");
        }
        catch(Exception e)
        {
            Log.d("ERROR", e.getMessage());
        }
    }

    ArrayList<String> pendingDownloads(String subject, int questionSet)
    {
        ArrayList<String> pendingList = new ArrayList<>();

        ArrayList<String> tableNames = new ArrayList<String>();
        if(subject.equalsIgnoreCase("ALL")) {
            tableNames.add("DOWNLOAD_STATUS_COMPUTERS");
            tableNames.add("DOWNLOAD_STATUS_MATHS");
            tableNames.add("DOWNLOAD_STATUS_GK");
            tableNames.add("DOWNLOAD_STATUS_SCIENCE");
            tableNames.add("DOWNLOAD_STATUS_MORALSCIENCE");
            tableNames.add("DOWNLOAD_STATUS_ENGLISH");
        }
        else
        {
            tableNames.add("DOWNLOAD_STATUS_" + subject.toUpperCase());
        }

        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);
            int firstSet;
            int lastSet;
            if(questionSet == 0) // means all sets
            {
                firstSet = Util.FIRST_SET;
                lastSet = Util.LAST_SET;
            }

            else
            {
                if(questionSet < Util.FIRST_SET || questionSet > Util.LAST_SET) // check for valid question set
                {
                    // not a valid question set, will download only set11 (the first set)
                    questionSet = Util.FIRST_SET;
                }
                firstSet = questionSet;
                lastSet = questionSet;
            }
            try {
                for (int set = firstSet; set <= lastSet; set++) {
                    Cursor cursor;
                    String columnName = "SET" + set;
                    String query = "select * from " + tableName + " where " + columnName + " = 0";
                    cursor = myDataBase.rawQuery(query, null);
                    if (cursor.getCount() > 0) // question not yet downloaded
                    {
                        String table = tableName.split("_")[2];
                        pendingList.add(table.toLowerCase() + "/" + columnName.toLowerCase());
                        deleteTableData("CBSE_" + table, set);
                    }
                    cursor.close();
                }
            }

            catch(Exception e)
            {
                Log.d("Error", Objects.requireNonNull(e.getMessage()));
            }
        }

        return pendingList;
    }

    int getLocalQuestionDatabaseVersion(String subject)
    {
        int localDatabaseVersion = 0;

        String query = "select " + subject + " from QUESTION_DATABASE_VERSION";

        Cursor cursor;

        try
        {
            cursor = myDataBase.rawQuery(query, null);
        }
        catch(Exception e)
        {
            return -1;
        }

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            localDatabaseVersion = cursor.getInt(0);
        }
        cursor.close();

        return localDatabaseVersion;
    }

    ArrayList<Integer> getDownloadedQuestionSets(String subject)
    {
        ArrayList<Integer> downloadedSet = new ArrayList<>();

        String tableName = "DOWNLOAD_STATUS_" + subject.toUpperCase();
        Cursor cursor;

        for(int set = Util.FIRST_SET; set <= Util.LAST_SET; set++)
        {
            String columnName = "SET" + set;
            String query = "select * from " + tableName + " where " + columnName + "= 1";
            try
            {
                cursor = myDataBase.rawQuery(query, null);
            }
            catch(Exception e)
            {
                Log.d("CBSE_4_Error", Objects.requireNonNull(e.getMessage()));
                return downloadedSet;
            }

            if(cursor.getCount() > 0)
            {
                downloadedSet.add(set);
            }
            cursor.close();
        }

        return downloadedSet;
    }
}

/*

 public boolean insertDataIntoUserTable(String uid)
    {
        try
        {
            createDataBase();
        }
        catch(IOException e){}

        try {
            openDataBase();
        }

        catch(Exception e)
        {
            Log.d("DatabaseError", e.getMessage());
        }
        String query = "insert into USER_DATA values('" + uid + "')";
        Log.d("QueryString", query);
        myDataBase.execSQL(query);
        close();
        return true;
    }

    public String getUserIdFromDatabase()
    {
        try
        {
            createDataBase();
        }
        catch(IOException e){}

        try {
            openDataBase();
        }

        catch(Exception e)
        {
            Log.d("DatabaseError", e.getMessage());
            return "";
        }

        Cursor cursor;
        String query = "select * from USER_DATA";

		try
        {
            cursor = myDataBase.rawQuery(query, null);
        }
        catch(Exception e)
        {
            return "";
        }
        String record = "";

        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            record = cursor.getString(0);
        }
        cursor.close();
        close();
        return record;
    }

    private void deleteDataInAllSubjectTable()
    {
        ArrayList<String> subjectTables = new ArrayList<String>();
        subjectTables.add("CBSE_COMPUTERS_4");
        subjectTables.add("CBSE_ENGLISH_4");
        subjectTables.add("CBSE_GK_4");
        subjectTables.add("CBSE_MATHS_4");
        subjectTables.add("CBSE_MORALSCIENCE_4");
        subjectTables.add("CBSE_SCIENCE_4");

        try
        {
            createDataBase();
        }
        catch(IOException e){}

        try {
            openDataBase();
        }

       catch(Exception e)
        {
            Log.d("DatabaseError", e.getMessage());
        }

        for(int i = 0; i < subjectTables.size(); i++)
        {
            String tableName = subjectTables.get(i);

            String query = "delete from " + tableName;
            try
            {
                myDataBase.execSQL(query);
                Log.d("DELETE", "DELETE Successful");
            }
            catch(Exception e)
            {
                Log.d("ERROR", e.getMessage());
            }
        }
       }

 */