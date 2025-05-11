package com.example.hello.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.example.hello.model.dto.SeatDTO;
import com.example.hello.model.dto.StudyRoomDTO;
import com.example.hello.model.entity.Seat;
import com.example.hello.model.entity.StudyRoom;
import com.example.hello.repository.ReservationRepository;
import com.example.hello.repository.SeatRepository;
import com.example.hello.repository.StudyRoomRepository;
import com.example.hello.service.impl.StudyRoomServiceImpl;

public class StudyRoomServiceTest {

    @Mock
    private StudyRoomRepository studyRoomRepository;

    @Mock
    private SeatRepository seatRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private SeatService seatService;

    @InjectMocks
    private StudyRoomServiceImpl studyRoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateStudyRoomSuccess() {
        // 准备测试数据
        StudyRoom room = new StudyRoom();
        room.setName("测试自习室");
        room.setLocation("测试位置");
        room.setCapacity(10);
        room.setOpenTime("08:00");
        room.setCloseTime("22:00");
        room.setMaxAdvanceDays(7);
        room.setStatus("AVAILABLE");

        // 模拟保存自习室
        StudyRoom savedRoom = new StudyRoom();
        savedRoom.setId("test-room-id");
        savedRoom.setName(room.getName());
        savedRoom.setLocation(room.getLocation());
        savedRoom.setCapacity(room.getCapacity());
        savedRoom.setOpenTime(room.getOpenTime());
        savedRoom.setCloseTime(room.getCloseTime());
        savedRoom.setMaxAdvanceDays(room.getMaxAdvanceDays());
        savedRoom.setStatus(room.getStatus());
        when(studyRoomRepository.save(any(StudyRoom.class))).thenReturn(savedRoom);

        // 模拟保存座位
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= room.getCapacity(); i++) {
            Seat seat = new Seat();
            seat.setId("seat-" + i);
            seat.setSeatNumber(String.format("%03d", i));
            seat.setStudyRoomId(savedRoom.getId());
            seat.setStatus("AVAILABLE");
            seat.setCreatedAt(System.currentTimeMillis());
            seats.add(seat);
        }
        when(seatRepository.saveAll(any())).thenReturn(seats);
        
        // 模拟查询座位
        when(seatRepository.findByStudyRoomId(anyString())).thenReturn(seats);
        
        // 模拟座位转换
        when(seatService.convertToDTO(any(Seat.class))).thenAnswer(invocation -> {
            Seat seat = invocation.getArgument(0);
            SeatDTO dto = new SeatDTO();
            dto.setId(seat.getId());
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setStatus(seat.getStatus());
            return dto;
        });

        try {
            // 执行测试
            ResponseEntity<?> response = studyRoomService.createStudyRoom(room);

            // 验证结果
            assertNotNull(response);
            assertEquals(200, response.getStatusCodeValue());
            assertTrue(response.getBody() instanceof StudyRoomDTO);
            StudyRoomDTO responseDTO = (StudyRoomDTO) response.getBody();
            assertEquals(savedRoom.getId(), responseDTO.getId());
            assertEquals(savedRoom.getName(), responseDTO.getName());
            assertEquals(savedRoom.getCapacity(), responseDTO.getSeats().size());
            
            // 验证自习室保存被调用
            verify(studyRoomRepository, times(1)).save(any(StudyRoom.class));
            
            // 验证座位保存被调用
            verify(seatRepository, times(1)).saveAll(any());
            
            // 验证座位查询被调用
            verify(seatRepository, times(1)).findByStudyRoomId(anyString());
            
            // 验证座位转换被调用
            verify(seatService, times(room.getCapacity())).convertToDTO(any(Seat.class));
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    void testCreateStudyRoomWithInvalidCapacity() {
        // 准备测试数据
        StudyRoom room = new StudyRoom();
        room.setName("测试自习室");
        room.setCapacity(0);  // 无效的容量

        // 执行测试
        ResponseEntity<?> response = studyRoomService.createStudyRoom(room);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        
        // 验证自习室和座位保存没有被调用
        verify(studyRoomRepository, never()).save(any(StudyRoom.class));
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    void testCreateStudyRoomWithMissingName() {
        // 准备测试数据
        StudyRoom room = new StudyRoom();
        room.setCapacity(10);
        room.setOpenTime("08:00");
        room.setCloseTime("22:00");

        // 执行测试
        ResponseEntity<?> response = studyRoomService.createStudyRoom(room);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        
        // 验证自习室和座位保存没有被调用
        verify(studyRoomRepository, never()).save(any(StudyRoom.class));
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    void testCreateStudyRoomWithSeatCreationFailure() {
        // 准备测试数据
        StudyRoom room = new StudyRoom();
        room.setName("测试自习室");
        room.setLocation("测试位置");
        room.setCapacity(10);
        room.setOpenTime("08:00");
        room.setCloseTime("22:00");

        // 模拟保存自习室
        StudyRoom savedRoom = new StudyRoom();
        savedRoom.setId("test-room-id");
        when(studyRoomRepository.save(any(StudyRoom.class))).thenReturn(savedRoom);

        // 模拟座位保存失败
        when(seatRepository.saveAll(any())).thenThrow(new RuntimeException("座位创建失败"));

        // 执行测试
        ResponseEntity<?> response = studyRoomService.createStudyRoom(room);

        // 验证结果
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        
        // 验证自习室保存被调用
        verify(studyRoomRepository, times(1)).save(any(StudyRoom.class));
        
        // 验证自习室删除被调用（因为座位创建失败）
        verify(studyRoomRepository, times(1)).delete(any(StudyRoom.class));
    }
} 