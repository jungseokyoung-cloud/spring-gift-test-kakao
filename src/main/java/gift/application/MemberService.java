package gift.application;

import gift.model.Member;
import gift.model.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(final CreateMemberRequest request) {
        return memberRepository.save(new Member(request.getName(), request.getEmail()));
    }
}
