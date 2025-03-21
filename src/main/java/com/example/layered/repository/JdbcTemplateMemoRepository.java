package com.example.layered.repository;

import com.example.layered.dto.MemoResponseDto;
import com.example.layered.entity.Memo;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository // 이 클래스를 자동으로 빈으로 등록
public class JdbcTemplateMemoRepository implements MemoRepository {

    // JdbcTemplate : 스프링이 제공하는 JDBC 데이터베이스 연결 및 쿼리 실행을 쉽게 도와주는 클래스
    // JdbcTemplate를 사용하는 이유 : SQL 실행을 간편하게 처리(PreparedStatement 생략 가능), 예외처리 자동지원, 트랜잭션 지원가능
    private final JdbcTemplate jdbcTemplate;

    // DataSource는 DB 연결 정보를 포함하는 객체
    public JdbcTemplateMemoRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 메모 저장
    @Override
    public MemoResponseDto saveMemo(Memo memo) {

        // SimpleJdbcInsert는 INSERT 쿼리를 자동 생성해주는 도구
        // INSERT INTO memo (title, contents) VALUES (?,?)같은 SQL직접 생성할 필요 없음.
        // INSERT Query를 직접 작성하지 않아도 된다.
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        //.withTable("memo") 저장할 테이블 이름을 meno로 지정, .usingGeneratedKeyColumns("id")는 AUTO_INCREMENT로 생성되는 id값을 자동으로 반환받도록 설정
        jdbcInsert.withTableName("memo").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", memo.getTitle());
        parameters.put("contents", memo.getContents());

        // .executeAndReturnKey() 저장 후 생성된 key값을 Number 타입으로 반환하는 메서드
        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));

        return new MemoResponseDto(key.longValue(), memo.getTitle(), memo.getContents());
    }

    // 메모 목록 조회
    @Override
    public List<MemoResponseDto> findAllMemos() {
        return jdbcTemplate.query("SELET * FROM memo", memoRowMapper());
    }

    // 메모 단 건 조회
    @Override
    public Optional<Memo> findMemoById(Long id) {
        // ?부분에 id 값이 들어감. 즉, memo 테이블에서 id가 특정 값과 일치하는 행을 조회함.
        List<Memo> result = jdbcTemplate.query("SELET * FROM WHERE id = ?", memoRowMapperV2(), id);

        // result(List)를 스트림으로 변형(.stream())해주고 findAny()는 스트림에서 아무 요소나 하나 가져오는 메서드 만약 비어있다면 Optional.empty() 반환
        return result.stream().findAny();
    }

    @Override
    public Memo findMemoByIdOrElseThrow(Long id) {
        List<Memo> result = jdbcTemplate.query("SELECT * FROM memo WHERE id = ?", memoRowMapperV2(), id);

        return result.stream().findAny().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Does not exist id = " + id));
    }

    @Override
    public int deleteMemo(Long id) {
        // DELETE 쿼리를 실행하여 영향을 받은 행(row) 개수를 반환
        return jdbcTemplate.update("DELETE FROM memo WHERE id = ?", id);
    }

    // 메모 전체 수정
    @Override
    public int updateMemo(Long id, String title, String contents) {
        //쿼리의 영향을 받은 row 수를 int로 변환한다.
        return jdbcTemplate.update("UPDATE memo SET title = ?, contents = ? WHERE id = ?", title, contents, id);
    }

    // 메모 제목 수정
    @Override
    public int updateTitle(Long id, String title) {
        return jdbcTemplate.update("UPDATE memo SET title = ? WHERE id = ?", title, id);
    }


    // 메모 목록조회
    private RowMapper<MemoResponseDto> memoRowMapper() {
        return new RowMapper<MemoResponseDto>(){

            @Override
            public MemoResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new MemoResponseDto(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("contents")
                );
            }
        };
    }

    // 메모 단건 조회
    private RowMapper<Memo> memoRowMapperV2() {
        return new RowMapper<Memo>() {
            @Override
            public Memo mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Memo(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("contents")
                );
            }
        };
    }


}
