package com.eventostec.api.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.repositories.EventRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CouponService couponService;

    public Event createEvent(EventRequestDTO data) {
        String imgUrl = null;

        if(data.image() != null) {
            imgUrl = this.uploadImage(data.image());
        }

        Event newEvent = new Event();
        newEvent.setTitle(data.title());
        newEvent.setDescription(data.description());
        newEvent.setEventUrl(data.eventUrl());
        newEvent.setDate(new Date(data.date()));
        newEvent.setImgUrl(imgUrl);
        newEvent.setRemote(data.remote());

        eventRepository.save(newEvent);

        if(!data.remote()) {
            this.addressService.createAddress(data, newEvent);
        }

        return newEvent;
    }

    public List<EventResponseDTO> getUpcomingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPages = this.eventRepository.findUpcomingEvents(new Date(), pageable);
        return eventsPages.map(event -> new EventResponseDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getAddress() != null ? event.getAddress().getCity() : "",
            event.getAddress() != null ? event.getAddress().getUf() : "",
            event.getRemote(),
            event.getEventUrl(),
            event.getImgUrl()
        )).stream().toList();
    }

    public List<EventResponseDTO> getFilteredEvents(
        int page,
        int size,
        String title,
        String city,
        String uf,
        Date startDate,
        Date endDate
    ) {

        Date currentDate = new Date();
        Date futureDate = new Date();
        futureDate.setTime(currentDate.getTime()+15_778_476_000L);
        title = (title != null) ? title : "";
        city = (city != null) ? city : "";
        uf = (uf != null) ? uf : "";
        startDate = (startDate != null) ? startDate : currentDate;
        endDate = (endDate != null) ? endDate : futureDate;

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPages = this.eventRepository.findFilteredEvents(title, city, uf, startDate, endDate, pageable);
        return eventsPages.map(event -> new EventResponseDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getAddress() != null ? event.getAddress().getCity() : "",
            event.getAddress() != null ? event.getAddress().getUf() : "",
            event.getRemote(),
            event.getEventUrl(),
            event.getImgUrl()
        )).stream().toList();
    }

    public EventDetailsDTO getEventDetails(UUID eventId) {
        Event event = this.eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<Coupon> coupons = couponService.consultCoupons(eventId, new Date());

        List<EventDetailsDTO.CouponDTO> couponDTOs = coupons.stream()
            .map(coupon -> new EventDetailsDTO.CouponDTO(
                coupon.getCode(),
                coupon.getDiscount(),
                coupon.getValid()
            )).collect(Collectors.toList());

        return new EventDetailsDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getDate(),
            event.getAddress() != null ? event.getAddress().getCity() : "",
            event.getAddress() != null ? event.getAddress().getUf() : "",
            event.getImgUrl(),
            event.getEventUrl(),
            event.getRemote(),
            couponDTOs
        );
    }

    // No projeto original é feito com conexão ao AWS S3, mas não usarei AWS, logo não haverá upload
    private String uploadImage(MultipartFile file) {
        return "none";
    }

}
