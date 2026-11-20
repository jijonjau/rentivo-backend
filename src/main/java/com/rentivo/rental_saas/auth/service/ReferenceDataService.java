package com.rentivo.rental_saas.auth.service;

import com.rentivo.rental_saas.auth.common.AccountType;
import com.rentivo.rental_saas.auth.common.ListingStatus;
import com.rentivo.rental_saas.auth.common.PropertyType;
import com.rentivo.rental_saas.auth.dto.FetchReferenceData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class ReferenceDataService {

    public List<FetchReferenceData> fetchAccountTypes() {
        return Arrays.stream(AccountType.values())
                .filter(type -> type != AccountType.PLATFORM)
                .map(type -> new FetchReferenceData(
                        type.getCode(),
                        type.name()
                ))
                .toList();
    }

    public List<FetchReferenceData> getPropertyTypes() {
        return Arrays.stream(PropertyType.values())
                .map(type -> new FetchReferenceData(
                        type.getCode(),
                        type.name()
                ))
                .toList();
    }

    public List<FetchReferenceData> getListingStatuses() {
        return Arrays.stream(ListingStatus.values())
                .map(status -> new FetchReferenceData(
                        status.getCode(),
                        status.name()
                ))
                .toList();
    }
}