package com.illunex.emsaasrestapi.license;

import com.illunex.emsaasrestapi.license.dto.ResponseLicenseDTO;
import com.illunex.emsaasrestapi.license.mapper.LicenseMapper;
import com.illunex.emsaasrestapi.license.vo.LicenseVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseService {
    private final LicenseMapper licenseMapper;
    private final ModelMapper modelMapper;

    /**
     * 라이선스 목록 조회
     * @return
     */
    public ResponseLicenseDTO.LicenseList getLicenses() {
        List<LicenseVO> licenses = licenseMapper.selectAllByActive();
        List<ResponseLicenseDTO.License> licenseList = modelMapper.map(licenses, new TypeToken<List<ResponseLicenseDTO.License>>() {}.getType());

        return new ResponseLicenseDTO.LicenseList() {{
            setLicenseList(licenseList);
        }};
    }
}
