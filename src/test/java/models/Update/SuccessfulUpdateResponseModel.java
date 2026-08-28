package models.Update;

public record SuccessfulUpdateResponseModel(Integer id,
                                            String username,
                                            String firstName,
                                            String lastName,
                                            String email,
                                            String remoteAddr) {}
