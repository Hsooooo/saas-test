package com.illunex.emsaasrestapi.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class Utils {

    public enum eLogType {
        USER,
        SYSTEM,
        SCRAP
    }

    public static Marker getLogMaker(eLogType type){
        switch(type){
            case USER:
                return MarkerFactory.getMarker(eLogType.USER.name());
            case SYSTEM:
            default:
                return MarkerFactory.getMarker(eLogType.SYSTEM.name());
        }
    }

    @Autowired
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String convertObjToString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        }catch (JsonProcessingException e){
            return String.valueOf(obj);
        }
    }

    public static String createFileName(String originalFileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
    }

    public static String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(ErrorCode.COMMON_INVALID_FILE_EXTENSION.getMessage());
        }
    }

    public static class AES256 {
        private static final String alg = "AES/CBC/PKCS5Padding";
//        private static final String key = "jsahfkh22ofzmx874821o209";
//        private static final String iv = key.substring(0, 16); // 16byte

        public static String encrypt(String key, String text) throws Exception {
            String iv = key.substring(0, 16);
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

            byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        }

        public static String decrypt(String key, String cipherText) throws Exception {
            String iv = key.substring(0, 16);
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, "UTF-8");
        }
    }

    /**
     * Null을 제외한 Entity 맵핑 함수
     * 수정할 데이터, 원본 데이터 순으로 넣으면 원본 데이터에 수정할 데이터가 복제된다.
     * @param src
     * @param target
     */
    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 임시 비밀번호 생성
     *
     * @param range 비밀번호 크기
     * @return 임시 비밀번호
     */
    public static String getTempPassword(int range) {
        // 규칙 : 10자리, 난수 및 특수문자 포함
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();

//        48 ~ 57 (숫자)
//        63 ~ 64 (?, @)
//        65 ~ 90 (대문자)
//        97 ~ 122 (소문자)
        List<Character> signList = new ArrayList<>();
        Map<Character, Integer> map = new HashMap<>();
        // 시작문자, 반복개수
        map.put('!', 6);
        map.put('0', 10);
        map.put('?', 28);
        map.put('a', 26);

        map.forEach((startChar, loopSize) -> {
            for (int i = startChar; i < startChar + loopSize; i++) {
                if (i != '"') {
                    signList.add((char) i);
                }
            }
        });

        // 만들기
        for (int i = 0; i < range; i++) {
            tempPassword.append(signList.get(random.nextInt(signList.size())));
        }
        return tempPassword.toString();
    }

    /**
     * 이메일 형식 체크
     *
     * @param email 체크할 이메일
     * @return 형식 적합 여부
     */
    public static boolean isEmailValidate(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        } else {
            return email.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$");
        }
    }

    /**
     * 콜백 받을 URL 체크 후 리턴해주는 함수
     * @param defaultDomain
     * @param corsList
     * @param certUrl
     * @return
     */
    public static String checkCorsRegistrationToReplaceUrl(String defaultDomain, String[] corsList, String certUrl){
        List<String> cors = Arrays.stream(corsList).collect(Collectors.toList());
        int lastSlashIdx = certUrl.lastIndexOf("/");
        String sendDomain = defaultDomain + certUrl.substring(lastSlashIdx);
        for (int i = 0; i < cors.size(); i++) {
            if (certUrl.startsWith(cors.get(i))) {
                // 등록된 URL이면..
                sendDomain = certUrl;
                break;
            }
        }
        return sendDomain;
    }

    /**
     * 인증 만료 검증
     * @param expireDate
     * @return true : 만료됨, false : 유효함
     */
    public static boolean isExpireCertDate(LocalDateTime expireDate){
        if (LocalDateTime.now().isAfter(expireDate)) {
            return true;
        }
        return false;
    }

    public static boolean isExpireCertDate(String expireDate){
        return isExpireCertDate(LocalDateTime.parse(expireDate));
    }

    /**
     * 비밀번호 랜덤 생성
     * 영문(대소문자), 숫자, 특수문자 전부 조합하여 16글자 생성
     */
    public static String randomPassword(int maxLength) {
//        비밀번호는 8~16자 이내 영문, 숫자, 특수 문자 중 2종류 이상을 조합하여 입력해주세요.
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();

//        48 ~ 57 (숫자)
//        63 ~ 64 (?, @)
//        65 ~ 90 (대문자)
//        97 ~ 122 (소문자)
        List<Character> signList = new ArrayList<>();
        Map<Character, Integer> map = new HashMap<>();
        // 시작문자, 반복개수
        map.put('!', 6);
        map.put('0', 10);
        map.put('?', 28);
        map.put('a', 26);

        map.forEach((startChar, loopSize) -> {
            for (int i = startChar; i < startChar + loopSize; i++) {
                if (i != '"') {
                    signList.add((char) i);
                }
            }
        });

        // 만들기
        for (int i = 0; i < maxLength; i++) {
            tempPassword.append(signList.get(random.nextInt(signList.size())));
        }

        if(!Pattern.matches("^(?!((?:[A-Za-z]+)|(?:[~!@#$%^&*()_+=]+)|(?:[0-9]+))$)[A-Za-z\\d~!@#$%^&*()_+=]{8,16}$", tempPassword)) {
            randomPassword(16);
        }

        return tempPassword.toString();
    }

    /**
     * CamelCase -> snake_case 변환
     */
    public static String camelCaseToSnakeCaseValue(String snakeCaseValue) {
        char[] charArray = snakeCaseValue.toCharArray();
        StringBuilder convertValue = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] >= 'A' && charArray[i] <= 'Z' && i + 1 < charArray.length) {
                convertValue.append("_").append((char) (charArray[i] + 32));
            } else {
                convertValue.append(charArray[i]);
            }
        }
        return convertValue.toString();
    }
}
