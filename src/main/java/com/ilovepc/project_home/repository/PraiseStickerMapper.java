package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.sticker.vo.BoardParam;
import com.ilovepc.project_home.web.sticker.vo.StampStickerParam;

@HomeMaster
public interface PraiseStickerMapper {
    int stampSticker(StampStickerParam param);

    int createBoard(BoardParam param);
}
