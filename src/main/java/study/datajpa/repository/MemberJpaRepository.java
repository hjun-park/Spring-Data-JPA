package study.datajpa.repository;

import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

	@PersistenceContext
	private EntityManager em;

	public Member save(Member member) {
		em.persist(member);
		return member;
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}

	// >> 05. Optional로 조회하는 기능
	public Optional<Member> findById(Long id) {
		Member member = em.find(Member.class, id);
		return Optional.ofNullable(member);
	}

	public Member find(Long id) {
		return em.find(Member.class, id);
	}

	public long count() {
		return em.createQuery("select count(m) from Member m", Long.class)
			.getSingleResult();
	}


	public void delete(Member member) {
		em.remove(member);
	}

	// >> 07. 지정 나이보다 많은 멤버 조회 (메소드 이름으로 쿼리 생성)
	// 문제점: 내가 이 코드를 직접 짜야한다. ( 귀찮음 ) - 이걸 해결 (09번)
	public List<Member> findByUsernameAndAgeBiggerThan(String username, int age) {
		return em.createQuery("select m from Member m where m.username = :username and m.age > :age")
			.setParameter("username", username)
			.setParameter("age", age)
			.getResultList();

	}


}
