package com.illunex.emsaasrestapi.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.illunex.emsaasrestapi.common.code.EnumCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResponseTossPayDTO {
    /**
     * 토스 페이먼츠 결제 승인
     */
    @Getter
    @Setter
    public static class TossPay {
        @JsonProperty("mId")
        private String mId; // 상점아이디(MID) 토스페이먼츠에서 발급. 최대 길이는 14자
        private String lastTransactionKey; // 마지막 거래의 키값입니다. 한 결제 건의 승인 거래와 취소 거래를 구분하는 데 사용됩니다. 예를 들어 결제 승인 후 부분 취소를 두 번 했다면 마지막 부분 취소 거래의 키값이 할당됩니다. 최대 길이는 64자입니다.
        private String paymentKey; // 결제의 키값. 최대 길이는 200자. 결제를 식별하는 역할로, 중복되지 않는 고유한 값
        private String orderId; // 주문번호. 결제 요청에서 내 상점이 직접 생성한 영문 대소문자, 숫자, 특수문자 -, _로 이루어진 6자 이상 64자 이하의 문자열
        private String orderName; // 구매상품. 최대 길이 100자
        private Integer taxExemptionAmount; // 과세를 제외한 결제 금액(컵 보증금 등)입니다. 이 값은 결제 취소 및 부분 취소가 되면 과세 제외 금액도 일부 취소되어 값이 바뀝니다. * 과세 제외 금액이 있는 카드 결제는 부분 취소가 안 됩니다.
        /**
         * status
         * 결제 처리 상태입니다. 아래와 같은 상태 값을 가질 수 있습니다. 상태 변화 흐름이 궁금하다면 흐름도를 살펴보세요.
         * 흐름도 참조: https://docs.tosspayments.com/reference/using-api/webhook-events#payment_status_changed
         * - READY: 결제를 생성하면 가지게 되는 초기 상태입니다. 인증 전까지는 READY 상태를 유지합니다.
         * - IN_PROGRESS: 결제수단 정보와 해당 결제수단의 소유자가 맞는지 인증을 마친 상태입니다. 결제 승인 API를 호출하면 결제가 완료됩니다.
         * - WAITING_FOR_DEPOSIT: 가상계좌 결제 흐름에만 있는 상태입니다. 발급된 가상계좌에 구매자가 아직 입금하지 않은 상태입니다.
         * - DONE: 인증된 결제수단으로 요청한 결제가 승인된 상태입니다.
         * - CANCELED: 승인된 결제가 취소된 상태입니다.
         * - PARTIAL_CANCELED: 승인된 결제가 부분 취소된 상태입니다.
         * - ABORTED: 결제 승인이 실패한 상태입니다.
         * - EXPIRED: 결제 유효 시간 30분이 지나 거래가 취소된 상태입니다. IN_PROGRESS 상태에서 결제 승인 API를 호출하지 않으면 EXPIRED가 됩니다.
         */
        private String status; // 결제 처리 상태
        private String requestedAt; // 결제가 일어난 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
        private String approvedAt; // 결제 승인이 일어난 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
        private Boolean useEscrow; // 에스크로 사용 여부입니다.
        private String cultureExpense; // 문화비(도서, 공연 티켓, 박물관·미술관 입장권 등) 지출 여부입니다. 계좌이체, 가상계좌 결제에만 적용됩니다. * 카드 결제는 항상 false로 돌아옵니다. 카드 결제 문화비는 카드사에 문화비 소득공제 전용 가맹점번호로 등록하면 자동으로 처리됩니다.
        private CardDto card; // 카드 결제 정보
        private VirtualAccountDto virtualAccount; // 가상계좌 결제 정보
        private TransferDto transfer; // 계좌이체 결제 관련 정보
        private MobilePhoneDto mobilePhone; // 휴대폰 결제 관련 정보
        private giftCertificateDto giftCertificate; // 상품권 결제 관련 정보
        private CashReceiptDto cashReceipt; // 현금 영수증 정보
        private CashReceiptsDto cashReceipts; // 현금영수증 발행 및 취소 이력이 담기는 배열정보. 순서 보장 X
        private DiscountDto discount; // 카드사 및 퀵계좌이체의 즉시 할인 프로모션 정보
        private List<CancellationDto> cancels; // 결제 취소 이력
        private String secret; // 웹훅을 검증하는 최대 50자 값입니다. 가상계좌 웹훅 이벤트 본문으로 돌아온 secret과 같으면 정상적인 웹훅입니다. 결제 상태 웹훅 이벤트로 돌아오는 Payment 객체의 secret과 같으면 정상적인 웹훅입니다.
        private String type; // 결제 타입 정보. (NORMAL: 일반결제, BILLING: 자동결제, BRANDPAY: 브랜드페이)
        private EasyPayDto easyPay; // 간편결제 정보
        private String country; // 결제한 국가입니다. ISO-3166의 두 자리 국가 코드 형식입니다.(참고: https://ko.wikipedia.org/wiki/ISO_3166-1_alpha-2)
        private FailureDto failure; // 결제 승인 실패 정보
        private Boolean isPartialCancelable; // 부분 취소 가능 여부입니다. 이 값이 false이면 전액 취소만 가능합니다.
        private ReceiptDto receipt; // 발행된 영수증 정보
        private CheckoutDto checkout; // 결제창 정보
        private String currency; // 결제할 때 사용한 통화
        private Integer totalAmount; // 총 결제 금액. 결제 취소 등 결제 상태가 변해도 최초 결제된 결제 금액으로 유지 됨.
        private Integer balanceAmount; // 취소할 수 있는 금액(잔고). 이 값은 결제 취소나 부분 취소가 되고 나서 남은 값입니다. 결제 상태 변화에 따라 vat, suppliedAmount, taxFreeAmount, taxExemptionAmount와 함께 값이 변함
        private Integer suppliedAmount; // 공급가액입니다. 결제 취소 및 부분 취소가 되면 공급가액도 일부 취소되어 값이 바뀝니다.
        private Integer vat; // 부가세
        private Integer taxFreeAmount; // 결제 금액 중 면세 금액입니다. 결제 취소 및 부분 취소가 되면 면세 금액도 일부 취소되어 값이 바뀝니다. * 일반 상점일 때는 면세 금액으로 0이 돌아옵니다. 면세 상점, 복합 과세 상점일 때만 면세 금액이 돌아옵니다. 더 자세한 내용은 세금 처리하기에서 살펴보세요
        private String metadata; // 결제 요청 시 SDK에서 직접 추가할 수 있는 결제 관련 정보입니다. 최대 5개의 키-값(key-value) 쌍입니다. 키는 [ , ] 를 사용하지 않는 최대 40자의 문자열, 값은 최대 500자의 문자열입니다.
        private String method; // 결제수단. 카드, 가상계좌, 간편결제, 휴대폰, 계좌이체, 문화상품권, 도서문화상품권, 게임문화상품권 중 하나
        private String version; // Payment 객체의 응답 버전
    }

    /**
     * 카드 결제 정보
     */
    @Getter
    @Setter
    public static class CardDto {
        /**
         * 간편결제 참고: https://docs.tosspayments.com/resources/glossary/easypay
         * 간편결제 응답 참고: https://docs.tosspayments.com/guides/v2/easypay-response
         * 카드사에 결제 요청한 금액입니다. 즉시 할인 금액(discount.amount)이 포함됩니다. * 간편결제에 등록된 카드로 결제했다면 간편결제 응답 확인 가이드를 참고하세요.
         */
        private BigDecimal amount; // 카드사 결제 요청 금액
        private String issuerCode; //카드 발급사 두 자리 코드입니다. 카드사 코드를 참고(https://docs.tosspayments.com/codes/org-codes#%EC%B9%B4%EB%93%9C%EC%82%AC-%EC%BD%94%EB%93%9C)
        private String acquirerCode; // 카드 매입사 두 자리 코드입니다. 카드사 코드를 참고(https://docs.tosspayments.com/codes/org-codes#%EC%B9%B4%EB%93%9C%EC%82%AC-%EC%BD%94%EB%93%9C)
        private String number; // 카드번호. 최대길이 20자. 일부 마스킹 처리
        @JsonProperty("installmentPlanMonths")
        private Integer installmentPlanMonths; // 할부 개월 수입니다. 일시불이면 0입니다.
        private String approveNo; // 카드사 승인 번호입니다. 최대 길이는 8자입니다
        private String useCardPoint; // 카드사 포인트 사용 여부입니다. * 일반 카드사 포인트가 아닌, 특수한 포인트나 바우처를 사용하면 할부 개월 수가 변경되어 응답이 돌아오니 유의해주세요
        private String cardType; // 카드 종류입니다. 신용, 체크, 기프트, 미확인 중 하나입니다. 구매자가 해외 카드로 결제했거나 간편결제의 결제 수단을 조합해서 결제했을 때 미확인으로 표시됩니다.
        private String ownerType; // 카드의 소유자 타입입니다. 개인, 법인, 미확인 중 하나입니다. 구매자가 해외 카드로 결제했거나 간편결제의 결제 수단을 조합해서 결제했을 때 미확인으로 표시됩니다.

        /**
         * 카드 결제의 매입 상태입니다. 아래와 같은 상태 값을 가질 수 있습니다.
         * - READY: 아직 매입 요청이 안 된 상태입니다.
         * - REQUESTED: 매입이 요청된 상태입니다.
         * - COMPLETED: 요청된 매입이 완료된 상태입니다.
         * - CANCEL_REQUESTED: 매입 취소가 요청된 상태입니다.
         * - CANCELED: 요청된 매입 취소가 완료된 상태입니다.
         */
        private String acquireStatus; // 카드 결제의 매입 상태
        private Boolean isInterestFree; // 무이자 할부의 적용 여부입니다.
        /**
         * 할부가 적용된 결제에서 할부 수수료를 부담하는 주체입니다. BUYER, CARD_COMPANY, MERCHANT 중 하나입니다.
         * - BUYER: 상품을 구매한 구매자가 할부 수수료를 부담합니다. 일반적인 할부 결제입니다.
         * - CARD_COMPANY: 카드사에서 할부 수수료를 부담합니다. 무이자 할부 결제입니다.
         * - MERCHANT: 상점에서 할부 수수료를 부담합니다. 무이자 할부 결제입니다.
         */
        @JsonProperty("interestPayer")
        private String interestPayer; // 할부가 적용된 결제에서 할부 수수료를 부담하는 주체
    }

    /**
     * 가상계좌 결제 정보
     */
    @Getter
    @Setter
    public static class VirtualAccountDto {
        private String accountType; // 가상계좌 타입을 나타냅니다. 일반, 고정 중 하나입니다
        private String accountNumber; // 발급된 계좌번호입니다. 최대 길이는 20자입니다
        private String bankCode; // 가상계좌 은행 두 자리 코드입니다. 은행 코드와 증권사 코드를 참고하세요.(참고: https://docs.tosspayments.com/codes/org-codes#%EC%A6%9D%EA%B6%8C%EC%82%AC-%EC%BD%94%EB%93%9C)
        private String customerName; // 가상계좌를 발급한 구매자명입니다. 최대 길이는 100자입니다.
        private String depositorName; // 가상계좌에 입금한 계좌의 입금자명입니다
        private String dueDate; // 입금 기한입니다. yyyy-MM-dd'T'HH:mm:ss ISO 8601 형식을 사용합니다

        /**
         * 환불 처리 상태입니다. 아래와 같은 상태 값을 가질 수 있습니다.
         * - NONE: 환불 요청이 없는 상태입니다.
         * - PENDING: 환불을 처리 중인 상태입니다.
         * - FAILED: 환불에 실패한 상태입니다.
         * - PARTIAL_FAILED: 부분 환불에 실패한 상태입니다.
         * - COMPLETED: 환불이 완료된 상태입니다.
         */
        private String refundStatus; // 환불 처리 상태입니다
        private Boolean expired; // 가상계좌의 만료 여부입니다.
        private String settlementStatus; // 정산 상태입니다. 정산이 아직 되지 않았다면 INCOMPLETED, 정산이 완료됐다면 COMPLETED 값이 들어옵니다.
        private RefundReceiveAccountDto refundReceiveAccount; // 결제위젯 가상계좌 환불 정보 입력 기능으로 받은 구매자의 환불 계좌 정보입니다.
    }

    /**
     * 결제위젯 가상계좌 환불 정보 입력 기능으로 받은 구매자의 환불 계좌 정보입니다.
     */
    @Getter
    @Setter
    public static class RefundReceiveAccountDto {
        private String bankCode; // 은행 코드
        private String accountNumber; // 계좌번호
        private String holderName; // 예금주 정보
    }

    /**
     * 계좌이체 결제 관련 정보
     */
    @Getter
    @Setter
    public static class TransferDto {
        /**
         * 은행코드: https://docs.tosspayments.com/codes/org-codes#%EC%9D%80%ED%96%89-%EC%BD%94%EB%93%9C
         * 증권사 코드: https://docs.tosspayments.com/codes/org-codes#%EC%A6%9D%EA%B6%8C%EC%82%AC-%EC%BD%94%EB%93%9C
         */
        private String bankCode; // 은행 두 자리 코드입니다. 은행 코드와 증권사 코드를 참고하세요.
        private String settlementStatus; // 정산 상태입니다. 정산이 아직 되지 않았다면 INCOMPLETED, 정산이 완료됐다면 COMPLETED 값이 들어옵니다.
    }

    /**
     * 휴대폰 결제 관련 정보
     */
    @Getter
    @Setter
    public static class MobilePhoneDto {
        private String customerMobilePhone; // 구매자가 결제에 사용한 휴대폰 번호입니다. - 없이 숫자로만 구성된 최소 8자, 최대 15자의 문자열입니다
        private String settlementStatus; // 정산 상태입니다. 정산이 아직 되지 않았다면 INCOMPLETED, 정산이 완료됐다면 COMPLETED 값이 들어옵니다.
        private String receiptUrl; // 휴대폰 결제 내역 영수증을 확인할 수 있는 주소입니다.
    }

    /**
     * 상품권 결제 관련 정보
     */
    @Getter
    @Setter
    public static class giftCertificateDto {
        private String approveNo; // 결제 승인번호입니다. 최대 길이는 8자입니다.
        private String settlementStatus; // 정산 상태입니다. 정산이 아직 되지 않았다면 INCOMPLETED, 정산이 완료됐다면 COMPLETED 값이 들어옵니다.
    }

    /**
     * 현금 영수증 정보
     */
    @Getter
    @Setter
    public static class CashReceiptDto {
        private String type; // 현금영수증의 종류입니다. 소득공제, 지출증빙 중 하나입니다.
        private String receiptKey; // 현금영수증의 키값입니다. 최대 길이는 200자입니다.
        private String issueNumber; // 현금영수증 발급 번호입니다. 최대 길이는 9자입니다.
        private String receiptUrl; // 발행된 현금영수증을 확인할 수 있는 주소입니다. 테스트 환경에서 영수증 URL은 생성되지만 실제 데이터는 제공되지 않습니다. 영수증 샘플은 결제 결과 안내 가이드에서 확인하세요.
        private BigDecimal amount; // 현금영수증 처리된 금액입니다.
        private BigDecimal taxFreeAmount; // 면세 처리된 금액입니다.
    }

    /**
     * 현금영수증 발행 및 취소 이력이 담기는 배열입니다. 순서는 보장되지 않습니다.
     * 예를 들어 결제 후 부분 취소가 여러 번 일어나면 모든 결제 및 부분 취소 건에 대한 현금영수증 정보를 담고 있습니다.
     * 퀵계좌이체를 제외한 다른 계좌이체의 결제수단의 경우, 결제 즉시 현금영수증 정보를 확인할 수 있습니다. 가상계좌는 구매자가 입금을 완료하면 현금영수증 정보를 확인할 수 있습니다.
     * 25.07.01 부터, 현금영수증 정보의 제공 방식이 결제 수단에 따라 달라집니다. 뱅크페이(금융결제원 계좌이체), 가상계좌, 브랜드페이-계좌 의 경우 현금영수증 정보가 즉시 제공되지만, 퀵계좌이체의 경우 Payment 객체의 receipt.url 을 통해서만 확인 가능합니다.
     * 결제가 이미 승인된 후 현금영수증 발급 요청 API로 발급한 현금영수증은 먼저 처리된 결제 정보와 연결되지 않아 값이 null입니다. 현금영수증 조회 API로 조회해주세요.
     * 현금영수증 가맹점이라면 결제했을 때 바로 발급됩니다. 발급을 원하지 않는다면 토스페이먼츠 고객센터(1544-7772, support@tosspayments.com)로 문의해주세요.
     */
    @Getter
    @Setter
    public static class CashReceiptsDto {
        private String receiptKey; // 현금영수증의 키값입니다. 최대 길이는 200자입니다.
        private String orderId; // 주문번호입니다. 결제 요청에서 내 상점이 직접 생성한 영문 대소문자, 숫자, 특수문자 -, _로 이루어진 6자 이상 64자 이하의 문자열입니다. 각 주문을 식별하는 역할로, 결제 데이터 관리를 위해 반드시 저장해야 합니다. 결제 상태가 변해도 orderId는 유지됩니다
        private String orderName; // 구매상품입니다. 예를 들면 생수 외 1건 같은 형식입니다. 최대 길이는 100자입니다.
        private String type; // 현금영수증의 종류입니다. 소득공제, 지출증빙 중 하나입니다
        private String issueNumber; // 현금영수증 발급 번호입니다. 최대 길이는 9자입니다
        private String receiptUrl; // 발행된 현금영수증을 확인할 수 있는 주소입니다. 테스트 환경에서 영수증 URL은 생성되지만 실제 데이터는 제공되지 않습니다. 영수증 샘플은 결제 결과 안내 가이드에서 확인하세요.
        private String businessNumber; // 현금영수증을 발급한 사업자등록번호입니다. 길이는 10자입니다.
        private String transactionType; // 현금영수증 발급 종류입니다. 현금영수증 발급(CONFIRM)·취소(CANCEL) 건을 구분합니다.
        private Integer amount; // 현금영수증 처리된 금액입니다.
        private Integer taxFreeAmount; // 면세 처리된 금액입니다.
        private String issueStatus; // 현금영수증 발급 상태입니다. 발급 승인 여부는 요청 후 1-2일 뒤 조회할 수 있습니다. IN_PROGRESS, COMPLETED, FAILED 중 하나입니다. 각 상태의 자세한 설명은 CashReceipt 객체에서 확인할 수 있습니다.
        private FailureDto failure; // 결제 실패 객체
        private String customerIdentityNumber; // 현금영수증 발급에 필요한 소비자 인증수단입니다. 현금영수증을 발급한 주체를 식별합니다. 최대 길이는 30자입니다. 현금영수증 종류에 따라 휴대폰 번호, 사업자등록번호, 현금영수증 카드 번호 등을 입력할 수 있습니다.
        private String requestedAt; // 현금영수증 발급 혹은 취소를 요청한 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
    }

    /**
     * 결제 취소 이력
     */
    @Getter
    @Setter
    public static class CancellationDto {
        private Integer cancelAmount; // 결제 취소 금액
        private String cancelReason; // 결제를 취소한 이유입니다. 최대 길이는 200자입니다.
        private Integer taxFreeAmount; // 취소된 금액 중 면세 금액입니다
        private Integer taxExemptionAmount; // 취소된 금액 중 과세 제외 금액(컵 보증금 등)입니다
        private Integer refundableAmount; // 결제 취소 후 환불 가능한 잔액입니다
        private Integer transferDiscountAmount; // 퀵계좌이체 서비스의 즉시할인에서 취소된 금액입니다
        private Integer easyPayDiscountAmount; // 간편결제 서비스의 포인트, 쿠폰, 즉시할인과 같은 적립식 결제수단에서 취소된 금액
        private String canceledAt; // 결제 취소가 일어난 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
        private String transactionKey; // 취소 건의 키값입니다. 여러 건의 취소 거래를 구분하는 데 사용됩니다. 최대 길이는 64자입니다.
        private String receiptKey; // 취소 건의 현금영수증 키값입니다. 최대 길이는 200자입니다
        private String cancelStatus; // 취소 상태입니다. DONE이면 결제가 성공적으로 취소된 상태입니다.
        private String cancelRequestId; // 취소 요청 ID입니다. 비동기 결제에만 적용되는 특수 값입니다. 일반결제, 자동결제(빌링), 페이팔 해외결제에서는 항상 null입니다
        private Integer cardDiscountAmount; // 카드 서비스의 즉시할인에서 취소된 금액입니다.
    }

    /**
     * 발행된 영수증 정보
     */
    @Getter
    @Setter
    public static class ReceiptDto {
        private String url;
    }

    /**
     * 결제창 정보
     */
    @Getter
    @Setter
    public static class CheckoutDto {
        private String url;
    }

    /**
     * 간편결제 정보
     * 구매자가 선택한 결제수단에 따라 amount, discountAmount가 달라집니다. 간편결제 응답 확인 가이드를 참고하세요.
     * https://docs.tosspayments.com/guides/v2/easypay-response
     */
    @Getter
    @Setter
    public static class EasyPayDto {
        private String provider; // 선택한 간편결제사 코드입니다.
        private BigDecimal amount; // 간편결제 서비스에 등록된 계좌 혹은 현금성 포인트로 결제한 금액입니다.
        private BigDecimal discountAmount; // 간편결제 서비스의 적립 포인트나 쿠폰 등으로 즉시 할인된 금액입니다.
    }

    /**
     * 결제 승인 실패 정보
     *
     */
    @Getter
    @Setter
    public static class FailureDto {
        private String code; // 오류 타입을 보여주는 에러 코드입니다.
        private String message; // 에러 메시지입니다. 에러 발생 이유를 알려줍니다. 최대 길이는 510자입니다.
    }

    /**
     * 카드사 및 퀵계좌이체의 즉시 할인 프로모션 정보
     * 즉시 할인 프로모션이 적용됐을 때만 생성됩니다.
     */
    @Getter
    @Setter
    public static class DiscountDto {
        private Integer amount; // 카드사 및 퀵계좌이체의 즉시 할인 프로모션을 적용한 결제 금액입니다.
    }

    /**
     * 자동결제 빌링키 발급
     */
    @Getter
    @Setter
    public static class issueBillingKeyDto {
        @JsonProperty("mId")
        private String mId;
        private String authenticatedAt; // 결제수단 인증 날짜
        private String method; // 결제수단
        private String customerKey; // 구매자 ID
        private String billingKey; // 자동결제에서 카드정보 대신 사용되는값. 최대 200자
        private CardDto card;
        private String cardCompany; // 카드 발급사
        private String cardNumber; //카드번호
        private TransferDto transfers;
    }

    @Setter
    @Getter
    @Builder
    public static class GenerateCustomerKeyDto {
        private String customerKey;
    }

    /**
     * 토스페이 빌링키 발급 요청용
     * @param <T>
     */
    @Getter
    @Setter
    public static class TossApiResponse<T> {
        private boolean success;
        private int statusCode;
        private T body;
        private String responseData;
    }

    /**
     * 결제 상세조회
     */
    @Getter
    @Setter
    public static class TossPayDetailDto {
        private String orderNumber; // 주문번호
        private String orderName; // 구매상품
        private Integer taxExemptionAmount; // 과세를 제외한 결제 금액(컵 보증금 등)입니다. 이 값은 결제 취소 및 부분 취소가 되면 과세 제외 금액도 일부 취소되어 값이 바뀝니다. * 과세 제외 금액이 있는 카드 결제는 부분 취소가 안 됩니다.
        /**
         * status
         * 결제 처리 상태입니다. 아래와 같은 상태 값을 가질 수 있습니다. 상태 변화 흐름이 궁금하다면 흐름도를 살펴보세요.
         * 흐름도 참조: https://docs.tosspayments.com/reference/using-api/webhook-events#payment_status_changed
         * - READY: 결제를 생성하면 가지게 되는 초기 상태입니다. 인증 전까지는 READY 상태를 유지합니다.
         * - IN_PROGRESS: 결제수단 정보와 해당 결제수단의 소유자가 맞는지 인증을 마친 상태입니다. 결제 승인 API를 호출하면 결제가 완료됩니다.
         * - WAITING_FOR_DEPOSIT: 가상계좌 결제 흐름에만 있는 상태입니다. 발급된 가상계좌에 구매자가 아직 입금하지 않은 상태입니다.
         * - DONE: 인증된 결제수단으로 요청한 결제가 승인된 상태입니다.
         * - CANCELED: 승인된 결제가 취소된 상태입니다.
         * - PARTIAL_CANCELED: 승인된 결제가 부분 취소된 상태입니다.
         * - ABORTED: 결제 승인이 실패한 상태입니다.
         * - EXPIRED: 결제 유효 시간 30분이 지나 거래가 취소된 상태입니다. IN_PROGRESS 상태에서 결제 승인 API를 호출하지 않으면 EXPIRED가 됩니다.
         */
        private String status; // 결제 처리 상태
        private String requestedAt; // 결제가 일어난 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
        private String approvedAt; // 결제 승인이 일어난 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
        private Boolean useEscrow; // 에스크로 사용 여부입니다.
        private String cultureExpense; // 문화비(도서, 공연 티켓, 박물관·미술관 입장권 등) 지출 여부입니다. 계좌이체, 가상계좌 결제에만 적용됩니다. * 카드 결제는 항상 false로 돌아옵니다. 카드 결제 문화비는 카드사에 문화비 소득공제 전용 가맹점번호로 등록하면 자동으로 처리됩니다.
        private CardDto card; // 카드 결제 정보
        private VirtualAccountDto virtualAccount; // 가상계좌 결제 정보
        private TransferDto transfer; // 계좌이체 결제 관련 정보
        private MobilePhoneDto mobilePhone; // 휴대폰 결제 관련 정보
        private giftCertificateDto giftCertificate; // 상품권 결제 관련 정보
        private CashReceiptDto cashReceipt; // 현금 영수증 정보
        private CashReceiptsDto cashReceipts; // 현금영수증 발행 및 취소 이력이 담기는 배열정보. 순서 보장 X
        private DiscountDto discount; // 카드사 및 퀵계좌이체의 즉시 할인 프로모션 정보
        private List<CancellationDto> cancels; // 결제 취소 이력
        private String type; // 결제 타입 정보. (NORMAL: 일반결제, BILLING: 자동결제, BRANDPAY: 브랜드페이)
        private EasyPayDto easyPay; // 간편결제 정보
        private String country; // 결제한 국가입니다. ISO-3166의 두 자리 국가 코드 형식입니다.(참고: https://ko.wikipedia.org/wiki/ISO_3166-1_alpha-2)
        private FailureDto failure; // 결제 승인 실패 정보
        private Boolean isPartialCancelable; // 부분 취소 가능 여부입니다. 이 값이 false이면 전액 취소만 가능합니다.
        private ReceiptDto receipt; // 발행된 영수증 정보
        private CheckoutDto checkout; // 결제창 정보
        private String currency; // 결제할 때 사용한 통화
        private Integer totalAmount; // 총 결제 금액. 결제 취소 등 결제 상태가 변해도 최초 결제된 결제 금액으로 유지 됨.
        private Integer balanceAmount; // 취소할 수 있는 금액(잔고). 이 값은 결제 취소나 부분 취소가 되고 나서 남은 값입니다. 결제 상태 변화에 따라 vat, suppliedAmount, taxFreeAmount, taxExemptionAmount와 함께 값이 변함
        private Integer suppliedAmount; // 공급가액입니다. 결제 취소 및 부분 취소가 되면 공급가액도 일부 취소되어 값이 바뀝니다.
        private Integer vat; // 부가세
        private Integer taxFreeAmount; // 결제 금액 중 면세 금액입니다. 결제 취소 및 부분 취소가 되면 면세 금액도 일부 취소되어 값이 바뀝니다. * 일반 상점일 때는 면세 금액으로 0이 돌아옵니다. 면세 상점, 복합 과세 상점일 때만 면세 금액이 돌아옵니다. 더 자세한 내용은 세금 처리하기에서 살펴보세요
        private String method; // 결제수단. 카드, 가상계좌, 간편결제, 휴대폰, 계좌이체, 문화상품권, 도서문화상품권, 게임문화상품권 중 하나
        private String version; // Payment 객체의 응답 버전
    }

    /**
     * 스케줄 결제 결과
     */
    @Getter
    @Setter
    @Builder
    public static class BillingSchedule {
        private Integer successCnt; // 결제 성공 개수
        private Integer failCnt; // 결제 실패 개수(결제 실패시 무료 라이센스로 변경)
        private Integer freeCnt; // 결제 실패가 아닌 결제 정보가 없거나 해지하여 무료 라이센스로 변경 개수
        private Integer unchangedCnt; // 라이센스 변경이 필요 없는 개수
        private Integer billingResultCnt; // 결제 성공 + 실패 + 무료 개수
        private Integer totalCnt; // 결제 성공 + 결제 실패 + 무료 라이센스로 변경 개수 + 변경이 필요없는 개수 총 합
    }

    /**
     * 토스페이 결제 또는 예약 유무
     */
    @Getter
    @Setter
    public static class TossPayResponse {
        private String stateCd; // PSC0001: 변경 예약, PSC0003: 변경 완료
        private String stateCdDesc;
        private String approvedAt; // 결제 승인이 일어난 날짜와 시간 정보입니다. yyyy-MM-dd'T'HH:mm:ss±hh:mm ISO 8601 형식입니다. (e.g. 2022-01-01T00:00:00+09:00)
        private BigDecimal amount; // 카드사 결제 요청 금액
        private String periodCd; // 라이센스 기간 구분(LPC0001: 월간, LPC0002: 연간, LPC0003: 무료)
        private String periodCdDesc;
        private String functionCd; // 라이센스 기능 구분(LFC0001: Free, LFC0002: Enterprise, LFC0003: Business)
        private String functionCdDesc;

        public void setStateCd(String code) {
            this.stateCd = code;
            this.stateCdDesc = EnumCode.getCodeDesc(code);
        }

        public void setPeriodCd(String code) {
            this.periodCd = code;
            this.periodCdDesc = EnumCode.getCodeDesc(code);
        }

        public void setFunctionCd(String code) {
            this.functionCd = code;
            this.functionCdDesc = EnumCode.getCodeDesc(code);
        }
    }

}