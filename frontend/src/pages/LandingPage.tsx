import { Button } from '../shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '../shared/components/ui/card';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '../shared/components/ui/accordion';
import { Avatar, AvatarFallback, AvatarImage } from '../shared/components/ui/avatar';
import { Badge } from '../shared/components/ui/badge';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Check } from 'lucide-react';

const LANDING_DATA = {
  heroTitle: "도장 관리의 모든 것,\nMARU 하나로 완성하다",
  heroSubtitle: "출석부터 수납, 원생 관리까지.\n관장님의 소중한 시간을 돌려드립니다.\n지금 바로 시작하세요.",
  heroImage: "https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=2000&auto=format&fit=crop",
  features: [
    {
      title: "스마트 출석체크",
      desc: "QR코드와 얼굴인식으로 빠르고 정확하게.\n등하원 알림은 기본입니다.",
      image: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=800&auto=format&fit=crop"
    },
    {
      title: "간편한 수납관리",
      desc: "복잡한 수납 업무를 자동화하세요.\n미납 알림도 알아서 챙겨드립니다.",
      image: "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?q=80&w=800&auto=format&fit=crop"
    },
    {
      title: "체계적인 원생관리",
      desc: "입관부터 승급까지,\n원생의 성장 데이터를 한눈에 파악하세요.",
      image: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=800&auto=format&fit=crop"
    }
  ],
  stats: [
    { label: "함께하는 도장", value: "500+" },
    { label: "관리중인 원생", value: "10,000+" },
    { label: "재계약률", value: "98%" },
    { label: "누적 출석체크", value: "1M+" }
  ],
  testimonials: [
    {
      name: "김민수 관장님",
      role: "청룡 태권도",
      content: "MARU 도입 후 학부모님들의 만족도가 정말 높아졌습니다. 출석 알림 덕분에 안심하시더라고요.",
      avatar: "https://api.dicebear.com/9.x/avataaars/svg?seed=Felix"
    },
    {
      name: "이영희 관장님",
      role: "백호 합기도",
      content: "수납 관리가 정말 편해졌어요. 미납 문자 보내는 스트레스에서 해방되었습니다.",
      avatar: "https://api.dicebear.com/9.x/avataaars/svg?seed=Aneka"
    },
    {
      name: "박철수 관장님",
      role: "한마음 검도",
      content: "사범님들과 업무 분담하기가 너무 좋습니다. 권한 설정 기능이 디테일해서 마음에 들어요.",
      avatar: "https://api.dicebear.com/9.x/avataaars/svg?seed=Jude"
    }
  ],
  pricing: [
    {
      name: "Basic",
      price: "무료",
      desc: "소규모 도장을 위한 핵심 기능",
      features: ["원생 50명까지", "출석체크", "기본 통계"]
    },
    {
      name: "Pro",
      price: "₩39,000",
      desc: "성장하는 도장을 위한 모든 기능",
      features: ["원생 무제한", "수납 관리", "알림톡 발송", "사범 관리"],
      popular: true
    },
    {
      name: "Enterprise",
      price: "별도 문의",
      desc: "프랜차이즈 및 대형 도장 맞춤",
      features: ["전담 매니저", "커스텀 기능 개발", "API 연동"]
    }
  ],
  faq: [
    {
      question: "설치비나 가입비가 있나요?",
      answer: "아니요, MARU는 초기 비용 없이 월 구독료로 이용하실 수 있습니다."
    },
    {
      question: "데이터를 옮길 수 있나요?",
      answer: "네, 엑셀 일괄 업로드 기능을 통해 기존 원생 데이터를 손쉽게 이관할 수 있습니다."
    },
    {
      question: "약정 기간이 있나요?",
      answer: "아니요, 언제든지 해지하실 수 있으며 위약금은 없습니다."
    },
    {
      question: "앱은 어디서 다운로드하나요?",
      answer: "구글 플레이스토어와 애플 앱스토어에서 'MARU'를 검색하시면 됩니다."
    }
  ]
};

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex flex-col">
      <section className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden text-center text-white">
        <div
          className="absolute inset-0 z-0 bg-cover bg-center bg-no-repeat"
          style={{ backgroundImage: `url(${LANDING_DATA.heroImage})` }}
        >
          <div className="absolute inset-0 bg-black/60 backdrop-blur-[2px]" />
        </div>

        <div className="container relative z-10 px-4 animate-in fade-in zoom-in duration-1000">
          <div className="mx-auto space-y-8">
            <div className="inline-flex items-center rounded-full border border-white/20 bg-white/10 px-3 py-1 text-sm backdrop-blur-md">
              <span className="flex h-2 w-2 rounded-full bg-green-400 mr-2 animate-pulse" />
              <span className="font-medium">스마트 도장 관리 시스템</span>
            </div>

            <h1 className="text-5xl font-extrabold leading-tight tracking-tight md:text-7xl lg:text-8xl drop-shadow-lg whitespace-pre-line">
              {LANDING_DATA.heroTitle}
            </h1>

            <p className="text-xl text-gray-200 md:text-2xl font-light leading-relaxed whitespace-pre-line drop-shadow-md">
              {LANDING_DATA.heroSubtitle}
            </p>

            <div className="flex flex-col items-center justify-center gap-4 sm:flex-row pt-8">
              <Button
                size="lg"
                className="h-14 px-12 text-lg font-bold bg-primary hover:bg-primary/90 shadow-lg hover:shadow-xl transition-all hover:-translate-y-1 w-full sm:w-auto"
                onClick={() => navigate('/login')}
              >
                무료로 시작하기
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>

        <div className="absolute bottom-10 left-1/2 -translate-x-1/2 animate-bounce text-white/50">
          <div className="h-10 w-6 rounded-full border-2 border-current flex justify-center pt-2">
            <div className="h-2 w-1 rounded-full bg-current" />
          </div>
        </div>
      </section>

      <section className="bg-primary py-16 text-primary-foreground">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-2 gap-8 md:grid-cols-4 text-center">
            {LANDING_DATA.stats.map((stat, index) => (
              <div key={index} className="space-y-2">
                <div className="text-4xl font-bold md:text-5xl">{stat.value}</div>
                <div className="text-sm opacity-80 md:text-base">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-32 bg-slate-50">
        <div className="container mx-auto px-4">
          <div className="mb-20 text-center">
            <h2 className="text-3xl font-bold tracking-tight text-slate-900 md:text-5xl">
              도장 운영의 <span className="text-primary">핵심 기능</span>
            </h2>
            <p className="mt-4 text-lg text-slate-600">
              복잡한 업무는 MARU에게 맡기고, 관장님은 교육에만 집중하세요.
            </p>
          </div>

          <div className="grid gap-12 md:grid-cols-3">
            {LANDING_DATA.features.map((feature, index) => (
              <Card key={index} className="group overflow-hidden border-none shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-2">
                <div className="aspect-video w-full overflow-hidden bg-slate-100">
                  <img
                    src={feature.image}
                    alt={feature.title}
                    className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
                  />
                </div>
                <CardHeader className="pb-2">
                  <CardTitle className="text-2xl font-bold text-slate-900">{feature.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-slate-600 leading-relaxed whitespace-pre-line">{feature.desc}</p>
                  <div className="mt-6 flex items-center text-primary font-medium opacity-0 transform translate-y-2 transition-all duration-300 group-hover:opacity-100 group-hover:translate-y-0">
                    자세히 보기 <ArrowRight className="ml-1 h-4 w-4" />
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="py-24 bg-white">
        <div className="container mx-auto px-4">
          <div className="mb-16 text-center">
            <h2 className="text-3xl font-bold tracking-tight md:text-4xl">관장님들의 생생한 후기</h2>
            <p className="mt-4 text-lg text-muted-foreground">이미 많은 관장님들이 MARU와 함께 성장하고 있습니다.</p>
          </div>
          <div className="grid gap-8 md:grid-cols-3">
            {LANDING_DATA.testimonials.map((testimonial, index) => (
              <Card key={index} className="bg-slate-50 border-none shadow-sm">
                <CardContent className="pt-6">
                  <div className="flex items-center gap-4 mb-4">
                    <Avatar>
                      <AvatarImage src={testimonial.avatar} />
                      <AvatarFallback>{testimonial.name[0]}</AvatarFallback>
                    </Avatar>
                    <div>
                      <p className="font-semibold">{testimonial.name}</p>
                      <p className="text-sm text-muted-foreground">{testimonial.role}</p>
                    </div>
                  </div>
                  <p className="text-slate-700">"{testimonial.content}"</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="py-24 bg-slate-50">
        <div className="container mx-auto px-4">
          <div className="mb-16 text-center">
            <h2 className="text-3xl font-bold tracking-tight md:text-4xl">합리적인 요금제</h2>
            <p className="mt-4 text-lg text-muted-foreground">도장 규모에 맞는 최적의 플랜을 선택하세요.</p>
          </div>
          <div className="grid gap-8 md:grid-cols-3 max-w-5xl mx-auto">
            {LANDING_DATA.pricing.map((plan, index) => (
              <Card key={index} className={`relative flex flex-col ${plan.popular ? 'border-primary shadow-xl scale-105 z-10' : 'border-border shadow-md'}`}>
                {plan.popular && (
                  <div className="absolute -top-4 left-1/2 -translate-x-1/2">
                    <Badge className="bg-primary text-primary-foreground px-3 py-1">Most Popular</Badge>
                  </div>
                )}
                <CardHeader>
                  <CardTitle className="text-2xl">{plan.name}</CardTitle>
                  <p className="text-sm text-muted-foreground">{plan.desc}</p>
                </CardHeader>
                <CardContent className="flex-1">
                  <div className="text-4xl font-bold mb-6">{plan.price}<span className="text-base font-normal text-muted-foreground">/월</span></div>
                  <ul className="space-y-3">
                    {plan.features.map((feature, i) => (
                      <li key={i} className="flex items-center gap-2">
                        <Check className="h-4 w-4 text-primary" />
                        <span className="text-sm">{feature}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
                <CardFooter>
                  <Button className="w-full" variant={plan.popular ? 'default' : 'outline'}>
                    시작하기
                  </Button>
                </CardFooter>
              </Card>
            ))}
          </div>
        </div>
      </section>

      <section className="py-24 bg-white">
        <div className="container mx-auto px-4 max-w-3xl">
          <div className="mb-16 text-center">
            <h2 className="text-3xl font-bold tracking-tight md:text-4xl">자주 묻는 질문</h2>
          </div>
          <Accordion type="single" collapsible className="w-full">
            {LANDING_DATA.faq.map((item, index) => (
              <AccordionItem key={index} value={`item-${index}`}>
                <AccordionTrigger className="text-left text-lg">{item.question}</AccordionTrigger>
                <AccordionContent className="text-slate-600">
                  {item.answer}
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </div>
      </section>

      <section className="py-24 bg-slate-900 text-white">
        <div className="container mx-auto px-4 text-center">
          <h2 className="mb-6 text-3xl font-bold md:text-5xl">지금 바로 MARU를 경험해보세요</h2>
          <p className="mb-10 text-xl text-slate-300">30일 무료 체험으로 모든 기능을 제한 없이 이용하실 수 있습니다.</p>
          <Button
            size="lg"
            variant="secondary"
            className="h-14 px-12 text-lg font-bold shadow-lg hover:shadow-xl transition-all hover:-translate-y-1"
            onClick={() => navigate('/login')}
          >
            무료 체험 시작하기
          </Button>
        </div>
      </section>

      <footer className="py-12 bg-slate-950 text-slate-400 border-t border-slate-800">
        <div className="container mx-auto px-4 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="text-center md:text-left">
            <span className="text-2xl font-bold text-white">MARU</span>
            <p className="mt-2 text-sm">스마트한 도장 관리의 시작</p>
          </div>
          <div className="flex gap-8 text-sm">
            <a href="#" className="hover:text-white transition-colors">이용약관</a>
            <a href="#" className="hover:text-white transition-colors">개인정보처리방침</a>
            <a href="#" className="hover:text-white transition-colors">고객센터</a>
          </div>
          <p className="text-xs">&copy; 2025 MARU. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
