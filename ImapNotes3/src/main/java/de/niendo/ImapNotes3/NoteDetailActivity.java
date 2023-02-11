package de.niendo.ImapNotes3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.NavUtils;

import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import de.niendo.ImapNotes3.Data.OneNote;
import de.niendo.ImapNotes3.Miscs.EditorMenuAdapter;
import de.niendo.ImapNotes3.Miscs.HtmlNote;
import de.niendo.ImapNotes3.Miscs.NDSpinner;
import de.niendo.ImapNotes3.Miscs.StickyNote;
import de.niendo.ImapNotes3.Miscs.Notifier;
import de.niendo.ImapNotes3.Miscs.Utilities;
import de.niendo.ImapNotes3.Sync.SyncUtils;

import java.io.File;
import java.util.HashMap;


import javax.mail.Message;

import jp.wasabeef.richeditor.RichEditor;


public class NoteDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //region Intent item names
    public static final String useSticky = "useSticky";
    public static final String selectedNote = "selectedNote";
    public static final String ActivityType = "ActivityType";
    public static final String ActivityTypeEdit = "ActivityTypeEdit";
    public static final String ActivityTypeAdd = "ActivityTypeAdd";
    //private static final int DELETE_BUTTON = 3;
    private static final int EDIT_BUTTON = 6;
    // --Commented out by Inspection (11/26/16 11:52 PM):private final static int ROOT_AND_NEW = 3;
    private static final String TAG = "IN_NoteDetailActivity";
    private boolean usesticky;
    @NonNull
    private String bgColor = "none";
    //private int realColor = R.id.yellow;
    private String suid; // uid as string
    private RichEditor editText;
    //endregion

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0); // or other
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.ActionBgColor)));
        // Don't display keyboard when on note detail, only if user touches the screen
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        Bundle extras = getIntent().getExtras();
        String stringres;
        String ChangeNote = extras.getString(ActivityType);
        if (ChangeNote.equals(ActivityTypeEdit)) {
            HashMap hm = (HashMap) extras.getSerializable(selectedNote);
            usesticky = extras.getBoolean(useSticky);

            if (hm != null) {
                suid = hm.get(OneNote.UID).toString();
                File rootDir = new File(getApplicationContext().getFilesDir(), hm.get(OneNote.ACCOUNT).toString());
                Message message = SyncUtils.ReadMailFromFileRootAndNew(suid, rootDir);
                //Log.d(TAG, "rootDir is null: " + (rootDir == null));
                Log.d(TAG, "rootDir: " + rootDir);
                if (message != null) {
                    if (usesticky) {
                        StickyNote stickyNote = StickyNote.GetStickyFromMessage(message);
                        stringres = stickyNote.text;
                        //String position = sticky.position;
                        bgColor = stickyNote.color;
                    } else {
                        HtmlNote htmlNote = HtmlNote.GetNoteFromMessage(message);
                        stringres = htmlNote.text;
                        bgColor = htmlNote.color;
                    }
                    SetupRichEditor();
                    editText.setHtml(stringres);
                } else {
                    // Entry can not opened..
                    Notifier.Show(R.string.sync_necessary, getApplicationContext(), 1);
                    finish();
                        return;
                    }
            } else { // Entry can not opened..
                Notifier.Show(R.string.Invalid_Message, getApplicationContext(), 1);
                finish();
                return;
            }
        } else if (ChangeNote.equals(ActivityTypeAdd)) {   // neuer Eintrag
            SetupRichEditor();

            if (extras.getBoolean("SHARE")) {
                String sharedText = extras.getString(Intent.EXTRA_TEXT);
                String type = extras.getString("TYPE");
                String subject = extras.getString(Intent.EXTRA_SUBJECT);
                if (subject != null) {
                    subject = "<b>" + subject + "</b><br>";
                } else subject = "";
                if (sharedText != null) {
                    if (type.equals("text/html")) {
                        editText.setHtml(subject + sharedText);
                    } else if (type.startsWith("text/")) {
                        editText.setHtml(Html.toHtml(new SpannedString(Html.fromHtml(subject + sharedText, Html.FROM_HTML_MODE_LEGACY)), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
                        //editText.setHtml(subject+sharedText);
                    } else if (type.startsWith("image/")) {
                        // toDo
                    }
                }
            }
        }
/*        // TODO: Watch for changes so that we can auto save.
        // See http://stackoverflow.com/questions/7117209/how-to-know-key-presses-in-edittext#14251047
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //here is your code
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Work in progess
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Work in progess
            }

        });
        */
        ResetColors();
    }

    private void SetupRichEditor() {
        // more functions, maybe use this editor...
        // https://github.com/Andrew-Chen-Wang/RichEditorView/blob/master/Sources/RichEditorView/Resources/editor/rich_editor.js
        // https://www.w3schools.com/howto/tryit.asp?filename=tryhow_js_collapsible_symbol
        editText = findViewById(R.id.bodyView);
        editText.setPadding(10, 10, 10, 10);
        //    mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        editText.setPlaceholder(getString(R.string.placeholder));
        editText.LoadFont("Alita Brush","Alita Brush.ttf");
/*
        mPreview = (TextView) findViewById(R.id.preview);
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            @Override public void onTextChange(String text) {
                mPreview.setText(text);
            }
        });

*/

        editText.setOnClickListener(new RichEditor.onClickListener() {
            @Override public void onClick(String text) {
                String url = Utilities.getValueFromJSON(text,"href");
                Notifier.Show(url,NoteDetailActivity.this,3);
            }
        });

        NDSpinner formatSpinner = findViewById(R.id.action_format);
        formatSpinner.setAdapter(new EditorMenuAdapter(NoteDetailActivity.this, R.layout.editor_row, new String[11], R.id.action_format, this));
        formatSpinner.setOnItemSelectedListener(this);

        NDSpinner insertSpinner = findViewById(R.id.action_insert);
        insertSpinner.setAdapter(new EditorMenuAdapter(NoteDetailActivity.this, R.layout.editor_row, new String[11], R.id.action_insert, this));
        insertSpinner.setOnItemSelectedListener(this);

        NDSpinner headingSpinner = findViewById(R.id.action_heading);
        headingSpinner.setAdapter(new EditorMenuAdapter(NoteDetailActivity.this, R.layout.editor_row, new String[8], R.id.action_heading, this));
        headingSpinner.setOnItemSelectedListener(this);

        NDSpinner alignmentSpinner = findViewById(R.id.action_alignment);
        alignmentSpinner.setAdapter(new EditorMenuAdapter(NoteDetailActivity.this, R.layout.editor_row, new String[6], R.id.action_alignment, this));
        alignmentSpinner.setOnItemSelectedListener(this);

        NDSpinner tableSpinner = findViewById(R.id.action_table);
        tableSpinner.setAdapter(new EditorMenuAdapter(NoteDetailActivity.this, R.layout.editor_row, new String[5], R.id.action_table, this));
        tableSpinner.setOnItemSelectedListener(this);

        findViewById(R.id.action_undo).setOnClickListener(v -> editText.undo());
        findViewById(R.id.action_redo).setOnClickListener(v -> editText.redo());

    }

/*
    // TODO: delete this?
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        //Boolean isClicked = true;
    }
*/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        NDSpinner spinner = (NDSpinner) parent;
        if ((view == null) || (!spinner.initIsDone)) return;
        switch (view.getId()) {
            case R.id.action_removeFormat:
                editText.removeFormat();
                break;
            case R.id.action_bold:
                editText.setBold();
                break;
            case R.id.action_italic:
                editText.setItalic();
                break;
            case R.id.action_subscript:
                editText.setSubscript();
                break;
            case R.id.action_superscript:
                editText.setSuperscript();
                break;
            case R.id.action_strikethrough:
                editText.setStrikeThrough();
                break;
            case R.id.action_underline:
                editText.setUnderline();
                break;
            case R.id.action_heading1:
                editText.setHeading(1);
                break;
            case R.id.action_heading2:
                editText.setHeading(2);
                break;
            case R.id.action_heading3:
                editText.setHeading(3);
                break;
            case R.id.action_heading4:
                editText.setHeading(4);
                break;
            case R.id.action_heading5:
                editText.setHeading(5);
                break;
            case R.id.action_heading6:
                editText.setHeading(6);
                break;
            case R.id.action_txt_color_white:
                editText.setTextColor("white");
                break;
            case R.id.action_txt_color_grey:
                editText.setTextColor("grey");
                break;
            case R.id.action_txt_color_black:
                editText.setTextColor("black");
                break;
            case R.id.action_txt_color_red:
                editText.setTextColor("FireBrick");
                break;
            case R.id.action_txt_color_green:
                editText.setTextColor("green");
                break;
            case R.id.action_txt_color_yellow:
                editText.setTextColor("yellow");
                break;
            case R.id.action_txt_color_brown:
                editText.setTextColor("brown");
                break;
            case R.id.action_txt_color_blue:
                editText.setTextColor("blue");
                break;
            case R.id.action_bg_color_white:
                editText.setTextBackgroundColor("white");
                break;
            case R.id.action_bg_color_grey:
                editText.setTextBackgroundColor("lightgrey");
                break;
            case R.id.action_bg_color_black:
                editText.setTextBackgroundColor("black");
                break;
            case R.id.action_bg_color_red:
                editText.setTextBackgroundColor("FireBrick");
                break;
            case R.id.action_bg_color_green:
                editText.setTextBackgroundColor("green");
                break;
            case R.id.action_bg_color_yellow:
                editText.setTextBackgroundColor("yellow");
                break;
            case R.id.action_bg_color_brown:
                editText.setTextBackgroundColor("brown");
                break;
            case R.id.action_bg_color_blue:
                editText.setTextBackgroundColor("blue");
                break;
            case R.id.action_font_size_1:
                editText.setFontSize(1);
                break;
            case R.id.action_font_size_2:
                editText.setFontSize(2);
                break;
            case R.id.action_font_size_3:
                editText.setFontSize(3);
                break;
            case R.id.action_font_size_4:
                editText.setFontSize(4);
                break;
            case R.id.action_font_size_5:
                editText.setFontSize(5);
                break;
            case R.id.action_font_size_6:
                editText.setFontSize(6);
                break;
            case R.id.action_font_size_7:
                editText.setFontSize(7);
                break;
            case R.id.action_font_serif:
                editText.setFontFamily("serif");
                break;
            case R.id.action_font_sansserif:
                editText.setFontFamily("sans-serif");
                break;
            case R.id.action_font_monospace:
                editText.setFontFamily("monospace");
                break;
            case R.id.action_font_cursive:
                editText.setFontFamily("cursive");
                break;
            case R.id.action_font_fantasy:
                editText.setFontFamily("Alita Brush");
                break;
            case R.id.action_indent:
                editText.setIndent();
                break;
            case R.id.action_outdent:
                editText.setOutdent();
                break;
            case R.id.action_align_left:
                editText.setAlignLeft();
                break;
            case R.id.action_align_center:
                editText.setAlignCenter();
                break;
            case R.id.action_align_right:
                editText.setAlignRight();
                break;
            case R.id.action_blockquote:
                editText.setBlockquote();
                break;
            case R.id.action_insert_bullets:
                editText.setBullets();
                break;
            case R.id.action_insert_numbers:
                editText.setNumbers();
                break;
            case R.id.action_insert_image:
                // 1. get the selected text via callback
                // 2. make the Image
                editText.setOnJSDataListener(new RichEditor.onJSDataListener() {
                    @Override public void onDataReceived(String value) {
                        if(!value.isEmpty()) {
                            String[] values = value.split(" ", 3);
                            if (values.length == 3)
                                editText.insertImage(values[0], values[1], values[2],"");
                            else if (values.length == 2)
                                editText.insertImage(values[0], values[1],"auto","");
                            else
                                editText.insertImage(value, "", "auto","");
                        }
                        else
                            Notifier.Show(R.string.select_link_image, getApplicationContext(), 1);
                    }
                });
                editText.getSelectedText();
                break;
            case R.id.action_insert_audio:
                // 1. get the selected text via callback
                // 2. make the Image
                editText.setOnJSDataListener(new RichEditor.onJSDataListener() {
                    @Override public void onDataReceived(String value) {
                        if(!value.isEmpty()) {
                                editText.insertAudio(value);
                        }
                        else
                            Notifier.Show(R.string.select_link_audio, getApplicationContext(), 1);
                    }
                });
                editText.getSelectedText();
                break;
            case R.id.action_insert_video:
                // 1. get the selected text via callback
                // 2. make the Image
                editText.setOnJSDataListener(new RichEditor.onJSDataListener() {
                    @Override public void onDataReceived(String value) {
                        if(!value.isEmpty()) {
                            if(value.startsWith("https://www.youtube.com"))
                                value = value.replace("watch?v=","embed/");
                            // https://www.youtube.com/watch?v=3AeYHDZ2riI
                            // https://www.youtube.com/embed/3AeYHDZ2riI
                           editText.insertVideo(value, "video", "auto", "");
                        }
                        else
                            Notifier.Show(R.string.select_link_video, getApplicationContext(), 1);
                    }
                });
                editText.getSelectedText();
                break;
            case R.id.action_insert_youtube:
                // 1. get the selected text via callback
                // 2. make the Image
                editText.setOnJSDataListener(new RichEditor.onJSDataListener() {
                    @Override public void onDataReceived(String value) {
                        if(!value.isEmpty()) {
                            if(value.startsWith("https://www.youtube.com"))
                                value = value.replace("watch?v=","embed/");

                            // https://www.youtube.com/watch?v=3AeYHDZ2riI
                            // https://www.youtube.com/embed/3AeYHDZ2riI

                             editText.insertYoutubeVideo(value);
                        }
                        else
                            Notifier.Show(R.string.select_link_youtube, getApplicationContext(), 1);
                    }
                });
                editText.getSelectedText();
                break;
            case R.id.action_insert_link:
                // 1. get the selected text via callback
                // 2. make the Link
                editText.setOnJSDataListener(new RichEditor.onJSDataListener() {
                    @Override public void onDataReceived(String value) {
                        if(!value.isEmpty()) {
                            String[] values = value.split(" ", 2);
                            if (values.length == 2)
                                editText.insertLink(values[0], values[0], values[1]);
                            else
                                editText.insertLink(value, value, value);
                        }
                        else
                            Notifier.Show(R.string.select_link, getApplicationContext(), 1);
                    }
                });
                editText.getSelectedText();
                break;
            case R.id.action_insert_checkbox:
                editText.insertCheckbox();
                break;
            case R.id.action_insert_star:
                editText.insertHTML("&#11088;");
                break;
            case R.id.action_insert_question:
                editText.insertHTML("&#10067;");
                break;
            case R.id.action_insert_exclamation:
                editText.insertHTML("&#10071;");
                break;
            case R.id.action_insert_hline:
                editText.insertHR_Line();
                break;
            case R.id.action_insert_section:
                editText.insertCollapsibleSection(getString(R.string.section), getString(R.string.content));
                break;
            case R.id.action_insert_table:
                editText.insertTable(3, 3);
                break;
            case R.id.action_insert_column:
                editText.addColumnToTable();
                break;
            case R.id.action_insert_row:
                editText.addRowToTable();
                break;
            case R.id.action_delete_column:
                editText.deleteColumnFromTable();
                break;
            case R.id.action_delete_row:
                editText.deleteRowFromTable();
                break;

        }

        // for color selection, it does not closes by itself
        NDSpinner formatSpinner = findViewById(R.id.action_format);
        formatSpinner.onDetachedFromWindow();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // realColor is misnamed.  It is the ID of the radio button widget that chooses the background
    // colour.
    private void ResetColors() {
        editText.setEditorBackgroundColor(Utilities.getColorByName(bgColor, getApplicationContext()));
        editText.setEditorFontColor(getColor(R.color.EditorTxtColor));
        (findViewById(R.id.scrollView)).setBackgroundColor(Utilities.getColorByName(bgColor, getApplicationContext()));
    }

    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuBuilder m = (MenuBuilder) menu;
        m.setOptionalIconsVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        //MenuItem item = menu.findItem(R.id.color);
        super.onPrepareOptionsMenu(menu);
        //depending on your conditions, either enable/disable
        //item.setVisible(usesticky);
        //menu.findItem(color.id).setChecked(true);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final Intent intent = new Intent();
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_note)
                        .setMessage(R.string.are_you_sure_you_wish_to_delete_the_note)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                            //Log.d(TAG,"We ask to delete Message #"+this.currentNote.get("number"));
                            intent.putExtra("DELETE_ITEM_NUM_IMAP", suid);
                            setResult(ListActivity.DELETE_BUTTON, intent);
                            finish();//finishing activity
                        })
                        .setNegativeButton(R.string.no, null).show();
                return true;
            case R.id.save:
                Save();
                return true;
            case R.id.share:
                Share();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.none:
                item.setChecked(true);
                bgColor = "none";
                ResetColors();
                return true;
            case R.id.blue:
                item.setChecked(true);
                bgColor = "blue";
                ResetColors();
                return true;
            case R.id.white:
                item.setChecked(true);
                bgColor = "white";
                ResetColors();
                return true;
            case R.id.black:
                item.setChecked(true);
                bgColor = "black";
                ResetColors();
                return true;
            case R.id.yellow:
                item.setChecked(true);
                bgColor = "yellow";
                ResetColors();
                return true;
            case R.id.pink:
                item.setChecked(true);
                bgColor = "pink";
                ResetColors();
                return true;
            case R.id.green:
                item.setChecked(true);
                bgColor = "green";
                ResetColors();
                return true;
            case R.id.brown:
                item.setChecked(true);
                bgColor = "brown";
                ResetColors();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Note that this function does not save the note to permanent storage it just passes it back to
     * the calling activity to be saved in whatever fashion it that activity wishes.
     */
    private void Save() {
        Log.d(TAG, "Save");
        editText.setOnJSDataListener(value -> {
            Intent intent = new Intent();
            intent.putExtra(ListActivity.EDIT_ITEM_NUM_IMAP, suid);
            Log.d(TAG, "Save html: " + value);
            intent.putExtra(ListActivity.EDIT_ITEM_TXT, value);
            intent.putExtra(ListActivity.EDIT_ITEM_COLOR, bgColor);
            setResult(NoteDetailActivity.EDIT_BUTTON, intent);
            finish();//finishing activity
        });

        // data comes via callback
        editText.getHtml();
    }

    private void Share() {
        Log.d(TAG, "Share");
        editText.setOnJSDataListener(value -> {
            Intent sendIntent = new Intent();
            Spanned html = Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT);
            String[] tok = html.toString().split("\n", 2);
            String title = getText(R.string.shared_note_from) + BuildConfig.APPLICATION_NAME + ": " + tok[0];

            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/html");
            sendIntent.putExtra(Intent.EXTRA_TEXT, html);
            //ToDo test: sendIntent.putExtra(Intent.EXTRA_TEXT, value);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);

            Intent shareIntent = Intent.createChooser(sendIntent, title);
            startActivity(shareIntent);
        });
        // data comes via callback
        editText.getHtml();

    }

// --Commented out by Inspection START (12/2/16 8:50 PM):
//    private void WriteMailToFile(@NonNull String suid, @NonNull Message message) {
//        String directory = getApplicationContext().getFilesDir() + "/" +
//                ListActivity.ImapNotesAccount.accountName;
//        try {
//            File outfile = new File(directory, suid);
//            OutputStream str = new FileOutputStream(outfile);
//            message.writeTo(str);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
// --Commented out by Inspection STOP (12/2/16 8:50 PM)


}
