package gift.ui;

import gift.application.CreateMemberRequest;
import gift.application.MemberService;
import gift.model.Member;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberRestController {

    private final MemberService memberService;

    public MemberRestController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public Member create(final CreateMemberRequest request) {
        return memberService.create(request);
    }
}
