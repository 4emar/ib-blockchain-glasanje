package network;

import java.io.Serializable;

public class MessageStruct extends Object implements Serializable {
    private static final long serialVersionUID = 3532734764930998421L;
    public int _code;
    public Object _content;

    public MessageStruct() {
        this._code = 0;
        this._content = null;
    }

    public MessageStruct(int code, Object content) {
        this._code = code;
        this._content = content;
    }
}
