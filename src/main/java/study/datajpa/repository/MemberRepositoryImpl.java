package study.datajpa.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RequiredArgsConstructor	// >> 29. 이 방식으로 injection 하는 걸 추천
public class MemberRepositoryImpl implements  MemberRepositoryCustom {

	@PersistenceContext
	private final EntityManager em;

	@Override
	public List<Member> findMemberCustom() {
		return em.createQuery("select m from Member m")
			.getResultList();
	}
}
