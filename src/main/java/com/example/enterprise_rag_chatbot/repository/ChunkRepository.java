package com.example.enterprise_rag_chatbot.repository;

import com.example.enterprise_rag_chatbot.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
   @Query(value = """
SELECT
    id,
    content,
    chunk_index
FROM chunks
ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
LIMIT :limit
""", nativeQuery = true)
List<Object[]> findNearestChunks(
        @Param("queryEmbedding") String queryEmbedding,
        @Param("limit") int limit);

 @Query(value = """
SELECT
    id,
    content,
    chunk_index,
    ts_rank(to_tsvector('english', content), plainto_tsquery('english', :query)) AS rank
FROM chunks
WHERE to_tsvector('english', content) @@ plainto_tsquery('english', :query)
ORDER BY rank DESC
LIMIT :limit
""", nativeQuery = true)
List<Object[]> findByKeywordSearch(
        @Param("query") String query,
        @Param("limit") int limit);
}