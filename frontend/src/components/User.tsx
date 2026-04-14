export type Role = "ADMIN" | "MODERATOR" | "USER";

export type UserData = {
    id: string;
    name: string;
    email: string;
    role: Role;
    token: string;
    createdAt: Date;
}

type UserApiResponse = {
  id: string;
  name: string;
  email: string;
  token: string;
  role: Role;
  createdAt: Date;
};

export const saveUser = (data: UserApiResponse) => {
  const user: UserData = {
    id: data.id,
    name: data.name,
    email: data.email,
    token: data.token,
    role: data.role,
    createdAt: data.createdAt,
  };

  localStorage.setItem("user", JSON.stringify(user));
  localStorage.setItem("token", data.token);
};