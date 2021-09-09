package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.repository.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// >> 01. 상속을 받아서 많은 기능들 구현이 가능함 제네릭 형식은 객체와 기본키타입
// 인터페이스인데 상속을 받았는데 구현체가 없다, 기능이 왜 되는지?
// private JpaRepository jpaRepository를 만들면 스프링 데이터 JPA에서
// 알아서 구현체를 자동으로 만들어서 인젝션을 해준다.
// 그래서 안에 내용이 없어도 된다.
// 여기는 @Repository가 없어도 인터페이스 인젝션을 알아서 해준다.
public interface MemberRepository extends JpaRepository<Member, Long> {

	// >> 09. 메소드 이름으로 쿼리 생성
	// 관례: UsernameAndAge를 쓰게되면 and 조건으로 바뀜
	// 형식: username= ? and age > ?
	// 더 많은 형식은 spring.io 접속 -> projects -> spring data ->
	//  spring data jpa -> learn탭 -> reference doc 열기 -> 5.3.2.Query Creation
	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

	// By하고 뒤에 아무것도 오지 않으면 전체 조회 (hello는 alias)
	List<Member> findHelloBy();

	// >> 11. @Query를 이용해서 JPA 인터페이스 메소드에 바로 적을 수 있다.
	// 메소드 이름으로 쿼리 생성이 복잡하게 메소드명이 정해질 때 대체방법으로 쿼리를 쓰는게 좋다.
	@Query("select m from Member m where m.username = :username and m.age = :age")
	List<Member> findMember(@Param("username") String username, @Param("age") int age);


	// 굉장히 편리하지만 이는 짤막짤막한 쿼리를 짤 때만 사용하는 것이 좋다.
	// 쿼리가 길게 나 올 것 같으면 @Query 사용 (JPQL)
	// 동적쿼리는 querydsl 쓴다. => 깔끔하고 유지보수가 좋다.

	// 유저네임만 필요한 경우
	@Query("select m.username from Member m")
	List<String> findUsernameList();

	// >> 12-1. DTO를 조회하고 싶을 때 (new 사용)
	@Query("select new study.datajpa.repository.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();

	// >> 13. 파라미터 바인딩 (컬렉션 이용)
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names")List<String> names);


	// >> 14. 반환 타입
	List<Member> findListByUsername(String username);	// 컬렉션
	Member findMemberByUsername(String name);	// 단건
	Optional<Member> findOptionalByUsername(String username);	// 단건 Optional

	// >> 15. 페이징
	// >> 16. totalCount @Query 따로 작성
	// totalcount Query는 단순히 username만 세서 계산하면 되는데
	// 그냥 @Query만 쓰면 모든걸 join하고 확인하기때문에 성능 저하 발생
	// 그래서 @Query 안에 countQuery라는걸 만들고 따로 totalCount 계산하는 전용 쿼리 작성
	@Query(value = "select m from Member m left join m.team t",
			countQuery = "select count(m.username) from Member m")
	Page<Member> findPageByAge(int age, Pageable pageable);

	Slice<Member> findSliceByAge(int age, Pageable pageable);

	// >> 18. 벌크성 수정 쿼리
	// >> 19. clearAutomatically = True // 사용하면 자동으로 쿼리날린 후 영속성 컨텍스트를 날려준다.
	@Modifying(clearAutomatically = true)	// 있으면 executeUpdate() 실행, 없으면 getResultList() 실행
			    // 수정을 할 것이므로 Modifying을 사용
	@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkAgePlus(@Param("age") int age);

	// >> 22. fetch 조인 ( 멤버를 조회할 때 연관된 팀을 다 긁어온다. )
	//  - fetch join은 JPQL을 써야하기 때문에 대신에 나온게 @EntityGraph
	@Query("select m from Member m left join fetch m.team")
	List<Member> findMemberFetchJoin();


	// >> 24. @EntityGraph : 내부적으로는 fetch Join과 유사하다.
	// @Query를 이용하여 JPQL을 사용하지 않고 편리하게 오버라이딩 해서 사용 가능
	@Override
	@EntityGraph(attributePaths = {"team"})	// 내부는 객체 이름
	List<Member> findAll();

	// >> 25. JPQL 쿼리 쓰면서 join은 fetch로 하고 싶을 때
	@EntityGraph(attributePaths = {"team"})
	@Query("select m from Member m")
	List<Member> findMemberEntityGraph();

	// >> 26. 메소드 이름으로도 @EntityGraph 사용 가능
	@EntityGraph(attributePaths = {"team"})
	List<Member> findEntityGraphByUsername(@Param("username") String username);

	// >> 27-1. Jpa Query Hint - 변경감지 없이 조회만 하는 메소드라면 성능 향상 목적으로 이용
	@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
	Member findReadOnlyByUsername(String username);

	// >> 28. Lock - db에서 select 할 때 다른 애들이 손대지 못하도록 lock을 걸 수 있다.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Member> findLockByUsername(String username);




	List<Member> findByUsername(String username);

}
