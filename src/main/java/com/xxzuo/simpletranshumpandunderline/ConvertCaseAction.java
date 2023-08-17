package com.xxzuo.simpletranshumpandunderline;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertCaseAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前的编辑器和选中的文本
        Editor editor = e.getDataContext().getData(CommonDataKeys.EDITOR);
        String selectedText = editor != null ? editor.getSelectionModel().getSelectedText() : null;
        if (selectedText == null) {
            Messages.showInfoMessage("未选择文本", "Info");
            return;
        }
        // 转换文本的驼峰形式和下划线形式
        String convertedText = convertText(selectedText);
        if (convertedText == null || "".equals(convertedText)) {
            return;
        }
        Document document = editor.getDocument();
        // 替换选中的文本  对文档进行操作部分代码，需要放入runnable，不然IDEA会卡住
        Runnable runnable = () -> document.replaceString(
                editor.getSelectionModel().getSelectionStart(),
                editor.getSelectionModel().getSelectionEnd(),
                convertedText
        );
        Project project = e.getProject();
        // 加入任务，由IDE调度任务
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    public String convertText(String text) {
        if (text == null || "".equals(text)) {
            return "";
        }
        if (text.contains("_")) {
            return underline2Hump(text, true);
        } else {
            return hump2Underline(text);
        }
    }

    /**
     * 下划线转驼峰
     *
     * @param text      待转换的词
     * @param smallHump 驼峰开头是否小写
     * @return 转换后的字符串
     */
    public String underline2Hump(String text, boolean smallHump) {
        if (text == null || "".equals(text)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("([A-Za-z\\d]+)(_)?");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(smallHump && matcher.start() == 0 ? Character.toLowerCase(word.charAt(0)) : Character.toUpperCase(word.charAt(0)));
            int index = word.lastIndexOf('_');
            if (index > 0) {
                sb.append(word.substring(1, index).toLowerCase());
            } else {
                sb.append(word.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰法转下划线
     *
     * @param text 待转换的词
     * @return 转换后的字符串
     */
    public String hump2Underline(String text) {
        if (text == null || "".equals(text)) {
            return "";
        }
        text = String.valueOf(text.charAt(0)).toUpperCase().concat(text.substring(1));
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("[A-Z]([a-z\\d]+)?");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            sb.append(word.toUpperCase());
            sb.append(matcher.end() == text.length() ? "" : "_");
        }
        return sb.toString();
    }


}
