package com.example.hotelreservation;

import com.example.hotelreservation.entity.Guest;
import com.example.hotelreservation.repository.GuestRepository;
import com.example.hotelreservation.service.GuestService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuestServiceTests {

    private final GuestRepository guestRepository = mock(GuestRepository.class);
    private final GuestService guestService = new GuestService(guestRepository);

    @Test
    void returnsAllGuestsWithTheirDetails() {
        Guest firstGuest = createGuest(1L, "Aysel Mammadova", "aysel@example.com", "+994501112233");
        Guest secondGuest = createGuest(2L, "Murad Aliyev", "murad@example.com", "+994502223344");
        List<Guest> guests = List.of(firstGuest, secondGuest);
        when(guestRepository.findAll()).thenReturn(guests);

        List<Guest> result = guestService.getAllGuests();

        assertSame(guests, result);
        assertGuestDetails(firstGuest, "Aysel Mammadova", "aysel@example.com", "+994501112233");
        assertGuestDetails(secondGuest, "Murad Aliyev", "murad@example.com", "+994502223344");
    }

    @Test
    void returnsGuestByIdWithTheirDetails() {
        Guest guest = createGuest(1L, "Aysel Mammadova", "aysel@example.com", "+994501112233");
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));

        Guest result = guestService.findById(1L);

        assertSame(guest, result);
        assertGuestDetails(result, "Aysel Mammadova", "aysel@example.com", "+994501112233");
    }

    @Test
    void throwsGuestNotFoundWhenIdDoesNotExist() {
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> guestService.findById(99L));

        assertEquals("Guest not found", exception.getMessage());
    }

    @Test
    void createsGuestAndSavesAllGuestDetails() {
        Guest guest = createGuest(null, "Aysel Mammadova", "aysel@example.com", "+994501112233");
        when(guestRepository.save(guest)).thenReturn(guest);

        Guest result = guestService.createGuest(guest);

        assertSame(guest, result);
        ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository).save(guestCaptor.capture());
        assertGuestDetails(guestCaptor.getValue(), "Aysel Mammadova", "aysel@example.com", "+994501112233");
    }

    @Test
    void deletesGuestById() {
        guestService.deleteGuest(1L);

        verify(guestRepository).deleteById(1L);
    }

    @Test
    void propagatesGuestNotFoundWhenRepositoryReportsMissingGuestDuringDelete() {
        doThrow(new RuntimeException("Guest not found")).when(guestRepository).deleteById(99L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> guestService.deleteGuest(99L));

        assertEquals("Guest not found", exception.getMessage());
    }

    private Guest createGuest(Long id, String fullName, String email, String phoneNumber) {
        Guest guest = new Guest();
        guest.setId(id);
        guest.setFullName(fullName);
        guest.setEmail(email);
        guest.setPhoneNumber(phoneNumber);
        return guest;
    }

    private void assertGuestDetails(Guest guest, String fullName, String email, String phoneNumber) {
        assertEquals(fullName, guest.getFullName());
        assertEquals(email, guest.getEmail());
        assertEquals(phoneNumber, guest.getPhoneNumber());
    }
}
