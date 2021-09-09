package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// >> 03. 가급적이면 ToString은 쓰지 않는게 좋다.
@ToString(of = {"id", "username", "age"})
public class Member {

	@Id @GeneratedValue
	@Column(name = "member_id")
	private Long id;
	private String username;
	private int age;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	// JPA 표준스펙에 엔티티는 default 생성자 필요
	// JPA가 프록시 해서 강제로 객체 만들어야하는데 private로 하면 만들지 못함
	// 그래서 protected 까지 열어준다.
	// NOARGS로 대체 가능
//	protected Member() {
//	}


	public Member(String username, int age) {
		this.username = username;
		this.age = age;
	}

	public Member(String username) {
		this.username = username;
	}

	// 멤버 팀 생성
	public Member(String username, int age, Team team) {
		this.username = username;
		this.age = age;
		if (team != null) {
			changeTeam(team);
		}
	}

	// 이런형식으로 엔티티 메소드 제공
	public void changeUsername(String username) {
		this.username = username;
	}

	// >> 04. 연관관계 편의 메소드
	// - 현재 멤버 객체의 팀을 변경할 뿐만 아니라 팀 객체도 바꿔준다.
	public void changeTeam(Team team) {
		this.team = team;
		team.getMembers().add(this);
	}
}
