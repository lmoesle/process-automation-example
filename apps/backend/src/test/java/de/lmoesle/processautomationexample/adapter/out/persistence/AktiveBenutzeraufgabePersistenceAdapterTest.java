package de.lmoesle.processautomationexample.adapter.out.persistence;

import de.lmoesle.processautomationexample.domain.tasklist.UserTaskTestdaten;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AktiveBenutzeraufgabePersistenceAdapterTest {

    private AktiveBenutzeraufgabeJpaRepository aktiveBenutzeraufgabeJpaRepository;
    private AktiveBenutzeraufgabePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        aktiveBenutzeraufgabeJpaRepository = mock(AktiveBenutzeraufgabeJpaRepository.class);
        adapter = new AktiveBenutzeraufgabePersistenceAdapter(aktiveBenutzeraufgabeJpaRepository);
    }

    @Test
    void storesTaskWhenItDoesNotExist() {
        when(aktiveBenutzeraufgabeJpaRepository.insertiereWennNichtVorhanden(UserTaskTestdaten.TASK_ID)).thenReturn(1);

        boolean gespeichert = adapter.speichereWennNeu(UserTaskTestdaten.taskId());

        assertThat(gespeichert).isTrue();
        verify(aktiveBenutzeraufgabeJpaRepository).insertiereWennNichtVorhanden(UserTaskTestdaten.TASK_ID);
    }

    @Test
    void ignoresTaskWhenItAlreadyExists() {
        when(aktiveBenutzeraufgabeJpaRepository.insertiereWennNichtVorhanden(UserTaskTestdaten.TASK_ID)).thenReturn(0);

        boolean gespeichert = adapter.speichereWennNeu(UserTaskTestdaten.taskId());

        assertThat(gespeichert).isFalse();
        verify(aktiveBenutzeraufgabeJpaRepository).insertiereWennNichtVorhanden(UserTaskTestdaten.TASK_ID);
    }

    @Test
    void removesTask() {
        adapter.entferne(UserTaskTestdaten.taskId());

        verify(aktiveBenutzeraufgabeJpaRepository).deleteById(UserTaskTestdaten.TASK_ID);
    }

    @Test
    void rejectsNullTaskIdWhenStoring() {
        assertThatThrownBy(() -> adapter.speichereWennNeu(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(aktiveBenutzeraufgabeJpaRepository);
    }

    @Test
    void rejectsNullTaskIdWhenRemoving() {
        assertThatThrownBy(() -> adapter.entferne(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("taskId darf nicht null sein");

        verifyNoInteractions(aktiveBenutzeraufgabeJpaRepository);
    }
}
