package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.entity.Member;

import java.util.List;

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

	// 굉장히 편리하지만 이는 짤막짤막한 쿼리를 짤 때만 사용하는 것이 좋다.
	// 쿼리가 길게 나올 것 같으면 JPQL 아니면 Querydsl 사용
}
