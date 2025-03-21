package com.example.layered.repository;

import com.example.layered.dto.MemoResponseDto;
import com.example.layered.entity.Memo;

import java.util.List;
import java.util.Optional;

public interface MemoRepository {

    MemoResponseDto saveMemo(Memo memo);

    List<MemoResponseDto> findAllMemos();

    // Optional을 사용하면 NullPointerException(NPE) 방지 가능
    Optional<Memo> findMemoById(Long id);

    int deleteMemo(Long id);

    int updateMemo(Long id, String title, String contents);

    int updateTitle(Long id, String title);

    Memo findMemoByIdOrElseThrow(Long id);

}
