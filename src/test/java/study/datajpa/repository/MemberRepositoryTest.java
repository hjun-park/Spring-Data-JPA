package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;
import study.datajpa.repository.dto.MemberDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	TeamRepository teamRepository;
	@PersistenceContext
	EntityManager em;

	@Test
	public void testMember() {
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		Member findMember = memberRepository.findById(savedMember.getId()).get();

		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	// >> 10. 메소드 이름으로 쿼리가 생성되는걸 테스트 ( 위 항목의 개선된 방법 )
	@Test
	public void findByUsernameAndAgeGreaterThan() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		// >> 08. [07코드에 대해서] 이름이 AAA이고 15보다 많은 사람 찾기
		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
	}

	// >> 12. @Query로 DTO 조회하기 테스트
	@Test
	public void findMemberDto() {

		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.setTeam(team);
		memberRepository.save(m1);

		List<MemberDto> memberDto = memberRepository.findMemberDto();
		for (MemberDto dto : memberDto) {
			System.out.println("dto = " + dto);
		}
	}

	// >> 14. 테스트
	@Test
	public void returnType() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		// 값이 없어도 리스트는 null을 반환하지 않기 때문에 그냥 반환하면 된다.
		List<Member> aaa = memberRepository.findListByUsername("AAA");
		System.out.println("aaa = " + aaa);

		// 값이 없는 경우 NULL을 반환한다.
		// exception 처리 하는 것보다 아래 Optional을 쓰는게 좋다.
		Member aaa1 = memberRepository.findMemberByUsername("AAA");
		System.out.println("aaa1 = " + aaa1);

		Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");
		System.out.println("aaa2 = " + aaa2); // aaa2.orElse()

		// 이외에도 Future(비동기실행), Stream, Slice, Page도 반환할 수 있다.
	}

	// >> 15. 페이징 테스트
	// 문제점: totalCount 는 전체를 훑어야 하기 때문에 성능문제 발생
	// 해결법: totalCount 쿼리 따로 분리하자 ( 16번 참고 )
	@Test
	public void paging() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		// 페이지는 0부터 시작함
		// 0페이지부터 시작하고 3개씩 가져오는데, username을 기준으로 뒤에서부터 sorting을 할거야
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		//when
		// >> 17. DTO도 아닌 엔티티 그대로 API 반환 해버리면 문제 발생
		// 그래서 map을 통해서 DTO로 반환해주는 것이 좋다.
		Page<Member> page = memberRepository.findPageByAge(age, pageRequest);
		Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));


		// 기존 페이징은 totalCount를 계산했지만 스프링 데이터 JPA에서는
		// 알아서 계산하고 totalCount를 계산하는 쿼리도 날려줌
		//	long totalCount = memberRepository.totalCount(age);

		// 페이징 계산 공식 적용
		// totalPage = totalCount / size
		// 마지막 페이지
		// 최초 페이지

		// then
		// getContent 쓰면 0부터 3개를 가져와서 content 저장
		List<Member> content = page.getContent();
		long totalElements = page.getTotalElements();	// totalCount와 같음

		assertThat(content.size()).isEqualTo(3);	// 가져온 페이지 ( 3개 )
		assertThat(page.getTotalElements()).isEqualTo(5); // totalCount (전체 개수(5개))
		assertThat(page.getNumber()).isEqualTo(0);	// 페이지 번호 (0번)
		assertThat(page.getTotalPages()).isEqualTo(2);	// 총 페이지 번호 2개 ( 5//3 + 1 )
		assertThat(page.isFirst()).isTrue();	// 첫 번째 페이지가 존재하는가
		assertThat(page.hasNext()).isTrue();	// 다음 페이지가 존재하는가
		assertThat(page.hasPrevious()).isFalse();



	}


	@Test
	public void slicing() {
		// given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		//when
		Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

		// then
		// slice는 주석처리된 기능들이 없다.
		List<Member> content = page.getContent();
//		long totalElements = page.getTotalElements();	// totalCount와 같음

		assertThat(content.size()).isEqualTo(3);	// 가져온 페이지 ( 3개 )
//		assertThat(page.getTotalElements()).isEqualTo(5); // totalCount (전체 개수(5개))
		assertThat(page.getNumber()).isEqualTo(0);	// 페이지 번호 (0번)
//		assertThat(page.getTotalPages()).isEqualTo(2);	// 총 페이지 번호 2개 ( 5//3 + 1 )
		assertThat(page.isFirst()).isTrue();	// 첫 번째 페이지가 존재하는가
		assertThat(page.hasNext()).isTrue();	// 다음 페이지가 존재하는가
		assertThat(page.hasPrevious()).isFalse();

	}

	// 1) slice는 page와 다르게 totalCount가 존재하지 않음
	// 2) 사이즈는 slice가 page보다 1 더 크다.
	// 3) 단순히 page -> slice 반환형 변경만으로 바꾸는게 가능하다.


	// >> 18. 벌크성 수정 쿼리 테스트
	@Test
	public void bulkUpdate() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		//when - 20보다 큰 나이들을 모두 한 살 더 +
		int resultCount = memberRepository.bulkAgePlus(20);
		// 해결법: bulk 연산 후에는 영속성 컨텍스트를 다 날린다.
		// 아래 날리는 것보다 더 좋은 방법이 있음 19번 참고
//		em.flush();
//		em.clear();

		// 벌크는 한 번에 바로 날려버림 해당 부분을 조심해야 한다.
		List<Member> result = memberRepository.findByUsername("member5");
		Member member5 = result.get(0);
		// 문제점: bulk 연산 후에 41로 된 것 같지만 실제로는 40이 출력된다.
		System.out.println("member5 = " + member5);


		//then	// 20, 21, 40 해서 총 3개가 수정됨
		assertThat(resultCount).isEqualTo(3);

	}

	@Test
	public void findMemberLazy() {
		//given
		//member1 -> teamA
		//member2 -> teamB

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 10, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		//when
		// >> 21. 여기서 문제 발생 (N+1)
		// select Member 1 ( 쿼리를 한 번 날렸는데 결과 쿼리가 더 나오는 것 )
//		List<Member> members = memberRepository.findAll();

		// >> 23. 개선안(fetch join)
		// Lazy는 team을 프록시로 가져왔지만,
		// fetch join을 하게되면 프록시가 아닌 team 내의 정보도 한 번에 가져온다.
		// 차이점: 일반 DB join의 경우 그냥 join만 하지만
		// fetch join은 join 뿐만 아니라 select 절에 관련 데이터(team)도 다 넣어준다.
		List<Member> members = memberRepository.findMemberFetchJoin();

		for (Member member : members) {
			// 멤버쿼리 -> 팀클래스(프록시객체) -> 팀 조회 쿼리 ( LAZY )
			System.out.println("member = " + member.getUsername());
			System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());

			System.out.println("member.team = " + member.getTeam().getName());
		}
	}


	// 24. fetch join 대신 EntityGraph 사용
	@Test
	public void findMemberLazyEntityGraph() {
		//given
		//member1 -> teamA
		//member2 -> teamB

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 10, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		//when
		List<Member> members = memberRepository.findAll();
//		List<Member> members = memberRepository.findEntityGraphByUsername("member1");

		for (Member member : members) {
			System.out.println("member = " + member.getUsername());
			System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());

			System.out.println("member.team = " + member.getTeam().getName());
		}
	}

	// >> 27. JPA Hint
	@Test
	public void queryHint() {
		//given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		//when
		// 실무에서는 get 바로 쓰면 안 됨
		// 이렇게 가져와서 이름을 변경하면 변경감지가 일어나고
//		Member findMember = memberRepository.findById(member1.getId()).get();
		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");	// QueryHint readonly는 변경되는 값 모두 무시
											// 결국 변경감지 체크를 안 하게 되고 그냥 넘어간다.

		// flush에 의해 update 쿼리가 날아간다.
		em.flush();

		// 단점: 변경감지는 데이터를 2개 가지고 있어야한다. (변경 전, 변경 후)
		// 메모리를 잡아먹게 되고, 체크하는 과정은 비용이 든다.
		// 만약에 나는 setUsername을 쓰지 않을 것이라고 해도
		// 이미 findById를 통해 가져오면 (변경 전, 변경 후) 데이터를 두 개 가지고 있게 된다.
		// 100% 조회용으로만 쓰겠다하면 최적화를 직접 해 준다.

	}

	// >> 28-1. lock 테스트 ( where 절에 username = ? for update 가 붙음 )
	@Test
	public void lock() {
		//given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		//when
		List<Member> result = memberRepository.findLockByUsername("member1");

		em.flush();
	}


}
