package cn.edu.zjut.quicknote.bean;

public class VoiceEntity {
    int start;
    int end;
    String voiceName;
    String voiceFlag;//<voice>name</voice>

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getVoiceFlag() {
        return voiceFlag;
    }

    public void setVoiceFlag(String voiceFlag) {
        this.voiceFlag = voiceFlag;
    }
}
