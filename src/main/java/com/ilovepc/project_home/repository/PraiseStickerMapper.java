package com.ilovepc.project_home.repository;

import com.ilovepc.project_home.config.rdb.annotation.HomeMaster;
import com.ilovepc.project_home.web.sticker.vo.BoardParam;
import com.ilovepc.project_home.web.sticker.vo.BoardResult;
import com.ilovepc.project_home.web.sticker.vo.StampStickerParam;
import com.ilovepc.project_home.web.sticker.vo.StampStickerResult;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@HomeMaster
public interface PraiseStickerMapper {
    @Select("CALL home_project.stamp_sticker(#{boardId},#{userNo},#{slotId},#{stickerUrl})")
    StampStickerResult stampSticker(StampStickerParam param);

    @Select("CALL home_project.board_ins(#{userNo},#{title},#{goal},#{totalSlots},#{rewardItem})")
    BoardResult createBoard(BoardParam param);

    List<BoardResult> getBoards(String status);
}
