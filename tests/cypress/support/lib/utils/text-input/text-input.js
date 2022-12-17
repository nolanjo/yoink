/// <reference types="cypress" />

import QlEditor from "./qleditor";
import TinyEditor from "./tiny-editor";

export { TINY_TEMPLATES, TINY_TOOLBAR_OPTION, TAB_LIST_OPTIONS } from "./tiny-editor";

export function inputText(textData, tinyEditor) {
    const editor = tinyEditor ? TinyEditor : QlEditor;
    if (textData.clearBody) {
        editor.clearTextBox();
    }
    editor.typeTextBox(textData.text);
    if (textData.embedded) {
        editor.attachFileToEditor(textData.embedded);
    }
    // need to handle inserts but only if using tiny editor
    if (tinyEditor && textData.template) {
        editor.insertTemplate(textData.template);
    }
}
