package com.hhmusic.lrc;

import java.util.List;


public interface ILrcParser {

    List<LrcRow> getLrcRows(String str);
}
