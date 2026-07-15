// Matches dto.ai.AiFindPartsResponse / ServicePart.

export interface ServicePart {
  url: string;
  title: string;
  description: string;
}

export interface AiFindPartsResponse {
  parts: ServicePart[];
}
