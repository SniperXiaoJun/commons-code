package code.ponfee.commons.mail;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FilenameUtils;

/**
 * 邮件信封实体
 * @author fupf
 */
public class MailEnvelope implements Serializable {

    private static final long serialVersionUID = 2375709603692620293L;
    private static final int MAX_LEN = 200;

    private final MailType type;
    private final String subject; // 主题
    private final Object content; // 内容
    private String[] to; // 接收人
    private String[] cc; // 抄送
    private String[] bcc; // 密送
    private String[] reply; // 回复

    /** 邮件附件：key为邮件附件名 */
    private final Map<String, DataSource> attachments = new HashMap<>();

    /** 正文图片：key为content-id，正文应包含：<img src="cid:content-id" /> */
    private final Map<String, DataSource> contentImages = new HashMap<>();

    private MailEnvelope(MailType type, String[] to, String subject, 
                         Object content, String[] cc, String[] bcc) {
        this.type = type;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.cc = cc;
        this.bcc = bcc;
    }

    /** text格式发送 */
    public static MailEnvelope newTextInstance(String to, String subject) {
        return newTextInstance(to, subject, null);
    }

    public static MailEnvelope newTextInstance(String to, String subject, String content) {
        return newTextInstance(new String[] { to }, subject, content);

    }

    public static MailEnvelope newTextInstance(String[] to, String subject) {
        return newTextInstance(to, subject, null);
    }

    public static MailEnvelope newTextInstance(String[] to, String subject, String content) {
        return newTextInstance(to, subject, content, null, null);
    }

    public static MailEnvelope newTextInstance(String[] to, String subject, String content,
        String[] cc, String[] bcc) {
        return new MailEnvelope(MailType.TEXT, to, subject, content, cc, bcc);
    }

    /** mime格式发送 */
    public static MailEnvelope newMimeInstance(String to, String subject, Object content) {
        return newMimeInstance(new String[] { to }, subject, content);
    }

    public static MailEnvelope newMimeInstance(String[] to, String subject, Object content) {
        return newMimeInstance(to, subject, content, null, null);
    }

    public static MailEnvelope newMimeInstance(String[] to, String subject, Object content,
        String[] cc, String[] bcc) {
        return new MailEnvelope(MailType.MIME, to, subject, content, cc, bcc);
    }

    /** setter */
    public void setTo(String[] to) {
        this.to = to;
    }

    public void setCc(String[] cc) {
        this.cc = cc;
    }

    public void setBcc(String[] bcc) {
        this.bcc = bcc;
    }

    public void setReply(String[] reply) {
        this.reply = reply;
    }

    /**
     * 添加附件
     * @param fileName
     * @param data
     */
    public void addAttachment(String fileName, byte[] data) {
        checkMimeType();
        if (this.attachments.containsKey(fileName)) {
            throw new IllegalArgumentException("repeated attachment filename: " + fileName);
        }
        this.attachments.put(fileName, buildDataSource(data));
    }

    public void addAttachment(String filepath) {
        addAttachment(new File(filepath));
    }

    public void addAttachment(String fileName, String filepath) {
        addAttachment(fileName, new File(filepath));
    }

    public void addAttachment(File file) {
        addAttachment(FilenameUtils.getName(file.getAbsolutePath()), file);
    }

    public void addAttachment(String fileName, File file) {
        checkMimeType();
        if (this.attachments.containsKey(fileName)) {
            throw new IllegalArgumentException("repeated attachment filename: " + fileName);
        }
        this.attachments.put(fileName, new FileDataSource(file));
    }

    /**
     * 添加正文图片
     * @param contentId
     * @param data
     */
    public void addContentImage(String contentId, byte[] data) {
        checkMimeType();
        if (this.contentImages.containsKey(contentId)) {
            throw new IllegalArgumentException("repeated image content-id: " + contentId);
        }
        this.contentImages.put(contentId, buildDataSource(data));
    }

    /**
     * 文件名为content-id
     * @param filepath
     */
    public void addContentImage(String filepath) {
        addContentImage(FilenameUtils.getName(filepath), filepath);
    }

    public void addContentImage(String contentId, String filepath) {
        addContentImage(contentId, new File(filepath));
    }

    public void addContentImage(File file) {
        addContentImage(FilenameUtils.getName(file.getAbsolutePath()), file);
    }

    public void addContentImage(String contentId, File file) {
        checkMimeType();
        if (this.contentImages.containsKey(contentId)) {
            throw new IllegalArgumentException("repeated image content-id: " + contentId);
        }
        this.contentImages.put(contentId, new FileDataSource(file));
    }

    /** getter */
    public MailType getType() {
        return type;
    }

    public String[] getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public Object getContent() {
        return content;
    }

    public String[] getCc() {
        return cc;
    }

    public String[] getBcc() {
        return bcc;
    }

    public String[] getReply() {
        return reply;
    }

    public Map<String, DataSource> getAttachments() {
        return attachments;
    }

    public Map<String, DataSource> getContentImages() {
        return contentImages;
    }

    private DataSource buildDataSource(byte[] data) {
        return new ByteArrayDataSource(data, "application/octet-stream");
    }

    private void checkMimeType() {
        if (this.type != MailType.MIME) {
            throw new IllegalArgumentException("operation must be mime type");
        }
    }

    @Override
    public String toString() {
        String cont = content == null ? "" : content.toString();
        return "[type=" + type + ", subject=" + substr(subject) + ", content=" + substr(cont)
            + ", to=" + arraystr(to) + ", cc=" + arraystr(cc) + ", bcc=" + arraystr(bcc)
            + ", attachments=" + substr(attachments) + ", contentImages=" + substr(contentImages) + "]";
    }

    private String arraystr(String[] array) {
        return Arrays.toString(array);
    }

    private String substr(String str) {
        if (str == null) return null;

        if (str.length() > MAX_LEN) {
            StringBuilder builder = new StringBuilder(str);
            builder.setLength(MAX_LEN - 3);
            str = builder.append("...").toString();
        }
        return str.toString();
    }

    private String substr(Object o) {
        if (o == null) return null;
        return substr(o.toString());
    }

    /**
     * 邮件类型 
     */
    static enum MailType {
        TEXT, MIME;
    }

}
