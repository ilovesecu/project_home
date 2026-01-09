package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.sticker.vo.BoardParam;
import com.ilovepc.project_home.web.sticker.vo.BoardResult;
import com.ilovepc.project_home.web.sticker.vo.StampStickerParam;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@HomeMaster
public interface PraiseStickerMapper {
    int stampSticker(StampStickerParam param);

    @Select("CALL home_project.board_ins(#{userNo},#{title},#{goal},#{totalSlots},#{rewardItem})")
    BoardResult createBoard(BoardParam param);

    List<BoardResult> getBoards(String status);
}
