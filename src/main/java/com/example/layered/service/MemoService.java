package com.example.layered.service;

import com.example.layered.dto.MemoRequestDto;
import com.example.layered.dto.MemoResponseDto;

public interface MemoService {

    MemoResponseDto saveMemo(MemoRequestDto requestDto);


}
