package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;

import de.seuhd.campuscoffee.domain.model.objects.DomainModel;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import de.seuhd.campuscoffee.domain.tests.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrudServiceTest {

    class TestDomain implements DomainModel<Long> {
        @Override
        public Long getId() {
            return null;
        }
    }

    @Mock
    CrudDataService<TestDomain, Long> dataService;

    @Mock
    TestDomain domain;

    CrudServiceImpl<TestDomain,Long> service;

    @BeforeEach
    void beforeEach() {
        service = new CrudServiceImpl<>(TestDomain.class) {
            @Override
            protected CrudDataService<TestDomain, Long> dataService() {
                return dataService;
            }
        };
    }

    @Test
    void clearDelegatesToDataService() {
        service.clear();

        verify(dataService).clear();
    }


    @Test
    void getAllReturnsDataServiceResult() {
        List<TestDomain> expected = List.of(domain);
        when(dataService.getAll()).thenReturn(expected);

        List<TestDomain> result = service.getAll();

        assertEquals(expected,result);
        verify(dataService).getAll();
    }

    @Test
    void getByIdReturnsEntity() {
        when(dataService.getById(1L)).thenReturn(domain);

        DomainModel<Long> result = service.getById(1L);

        assertEquals(domain, result);
        verify(dataService).getById(1L);
    }


    @Test
    void upsertCreatesEntityWhenIdIsNull() {
        when(domain.getId()).thenReturn(null);
        when(dataService.upsert(domain)).thenReturn(domain);

        DomainModel<Long> result = service.upsert(domain);

        assertEquals(domain, result);
        verify(dataService).upsert(domain);
        verify(dataService, never()).getById(any());
    }

    @Test
    void upsertUpdatesEntityWhenIdExists() {
        when(domain.getId()).thenReturn(1L);
        when(dataService.getById(1L)).thenReturn(domain);
        when(dataService.upsert(domain)).thenReturn(domain);

        DomainModel<Long> result = service.upsert(domain);

        assertEquals(domain, result);
        verify(dataService).getById(1L);
        verify(dataService).upsert(domain);
    }

    @Test
    void upsertThrowsDuplicationException() {
        when(domain.getId()).thenReturn(null);
        when(dataService.upsert(domain))
                .thenThrow(new DuplicationException(
                        TestDomain.class,
                        "duplicate",
                        null
                ));

        assertThrows(DuplicationException.class, () -> service.upsert(domain));
    }

    @Test
    void deleteDelegatesToDataService() {
        service.delete(1L);

        verify(dataService).delete(1L);
    }

}
